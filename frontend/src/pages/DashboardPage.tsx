import { useEffect, useState } from 'react'
import { Card, Col, Row, Statistic, Typography, Spin, message, Progress } from 'antd'
import {
  FileTextOutlined, EuroOutlined, CheckCircleOutlined,
  RobotOutlined, TeamOutlined, AppstoreOutlined, ClockCircleOutlined,
} from '@ant-design/icons'
import {
  PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer,
  BarChart, Bar, XAxis, YAxis, CartesianGrid,
} from 'recharts'
import type { Stats } from '../types/api'

const { Title, Text } = Typography

const STATUS_COLORS: Record<string, string> = {
  'Brouillon':   '#d9d9d9',
  'À compléter': '#fa8c16',
  'À valider':   '#1677ff',
  'Prêt':        '#52c41a',
  'Rejeté':      '#ff4d4f',
}

function KpiCard({
  title, value, suffix, icon, color, precision = 0,
}: {
  title: string
  value: number
  suffix?: string
  icon: React.ReactNode
  color: string
  precision?: number
}) {
  return (
    <Card>
      <Statistic
        title={
          <span style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            <span style={{ color, fontSize: 18 }}>{icon}</span>
            {title}
          </span>
        }
        value={value}
        precision={precision}
        suffix={suffix}
        valueStyle={{ color, fontWeight: 700 }}
      />
    </Card>
  )
}

export default function DashboardPage() {
  const [stats, setStats]     = useState<Stats | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetch('/api/stats')
      .then(r => {
        if (!r.ok) throw new Error(`HTTP ${r.status}`)
        return r.json()
      })
      .then((data: Stats) => setStats(data))
      .catch(() => message.error('Impossible de charger les statistiques'))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <Spin size="large" style={{ display: 'block', marginTop: 80, textAlign: 'center' }} />
  if (!stats)  return null

  // Confiance : le backend stocke en 0-1, on convertit en pourcentage
  const confidencePct = stats.tauxConfidenceMoyen <= 1
    ? Math.round(stats.tauxConfidenceMoyen * 100)
    : Math.round(stats.tauxConfidenceMoyen)

  const pieData = Object.entries(stats.devisParStatut).map(([name, value]) => ({ name, value }))

  const barData = Object.entries(stats.devisParStatut).map(([name, value]) => ({
    statut: name,
    count: value,
    fill: STATUS_COLORS[name] ?? '#8884d8',
  }))

  const mins = stats.tempsMoyenValidationMinutes
  const tempsMoyen = mins < 60
    ? `${Math.round(mins)} min`
    : mins < 1440
    ? `${(mins / 60).toFixed(1)} h`
    : `${(mins / 1440).toFixed(1)} j`

  return (
    <div style={{ padding: 24 }}>
      <Title level={3} style={{ marginBottom: 24 }}>Tableau de bord</Title>

      {/* ── KPI row ── */}
      <Row gutter={[16, 16]} style={{ marginBottom: 24 }}>
        <Col xs={24} sm={12} lg={6}>
          <KpiCard
            title="Total devis"
            value={stats.totalDevis}
            icon={<FileTextOutlined />}
            color="#1677ff"
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <KpiCard
            title="Montant moyen HT"
            value={stats.montantMoyenHT}
            suffix="€"
            precision={2}
            icon={<EuroOutlined />}
            color="#52c41a"
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <KpiCard
            title="Taux de validation"
            value={stats.tauxValidation}
            suffix="%"
            precision={1}
            icon={<CheckCircleOutlined />}
            color="#722ed1"
          />
        </Col>
        <Col xs={24} sm={12} lg={6}>
          <KpiCard
            title="Confiance IA moyenne"
            value={confidencePct}
            suffix="%"
            precision={0}
            icon={<RobotOutlined />}
            color="#fa8c16"
          />
        </Col>
      </Row>

      {/* ── Charts + secondary stats ── */}
      <Row gutter={[16, 16]}>

        {/* Pie chart — répartition par statut */}
        <Col xs={24} lg={10}>
          <Card title="Répartition par statut">
            {pieData.length > 0 ? (
              <ResponsiveContainer width="100%" height={280}>
                <PieChart>
                  <Pie
                    data={pieData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    paddingAngle={3}
                    dataKey="value"
                    label={({ name, percent }) =>
                      `${name} (${(percent * 100).toFixed(0)}%)`
                    }
                  >
                    {pieData.map(entry => (
                      <Cell
                        key={entry.name}
                        fill={STATUS_COLORS[entry.name] ?? '#8884d8'}
                      />
                    ))}
                  </Pie>
                  <Tooltip formatter={(v: number) => [`${v} devis`, '']} />
                  <Legend />
                </PieChart>
              </ResponsiveContainer>
            ) : (
              <Text type="secondary">Aucun devis enregistré.</Text>
            )}
          </Card>
        </Col>

        {/* Bar chart — nb de devis par statut */}
        <Col xs={24} lg={14}>
          <Card title="Devis par statut">
            {barData.length > 0 ? (
              <ResponsiveContainer width="100%" height={280}>
                <BarChart data={barData} margin={{ top: 5, right: 20, left: 0, bottom: 5 }}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="statut" tick={{ fontSize: 12 }} />
                  <YAxis allowDecimals={false} />
                  <Tooltip formatter={(v: number) => [`${v} devis`, 'Quantité']} />
                  <Bar dataKey="count" radius={[4, 4, 0, 0]}>
                    {barData.map(entry => (
                      <Cell key={entry.statut} fill={entry.fill} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <Text type="secondary">Aucun devis enregistré.</Text>
            )}
          </Card>
        </Col>

        {/* Secondary stats */}
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title={
                <span><TeamOutlined style={{ color: '#1677ff', marginRight: 6 }} />Clients</span>
              }
              value={stats.totalClients}
              valueStyle={{ color: '#1677ff' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title={
                <span><AppstoreOutlined style={{ color: '#52c41a', marginRight: 6 }} />Produits catalogue</span>
              }
              value={stats.totalProduits}
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={8}>
          <Card>
            <Statistic
              title={
                <span><ClockCircleOutlined style={{ color: '#722ed1', marginRight: 6 }} />Temps moyen validation</span>
              }
              value={tempsMoyen}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>

        {/* Confidence progress bar */}
        <Col xs={24}>
          <Card title="Précision de l'IA (confiance moyenne)">
            <Progress
              percent={confidencePct}
              strokeColor={
                confidencePct >= 75 ? '#52c41a'
                : confidencePct >= 50 ? '#fa8c16'
                : '#ff4d4f'
              }
              size="default"
              format={p => `${p}%`}
            />
            <Text type="secondary" style={{ display: 'block', marginTop: 8 }}>
              Score moyen de confiance sur l'ensemble des devis générés par l'IA.
            </Text>
          </Card>
        </Col>

      </Row>
    </div>
  )
}
