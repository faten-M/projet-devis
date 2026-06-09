import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Card, Row, Col, Table, InputNumber, Button, Tag, Alert,
  Typography, Space, Statistic, Modal, Input, Select,
  message, Spin, Descriptions, List, Collapse, DatePicker
} from 'antd'
import dayjs from 'dayjs'
import { MailOutlined } from '@ant-design/icons'
import {
  ArrowLeftOutlined, CheckOutlined, CloseOutlined,
  WarningOutlined, InfoCircleOutlined, FilePdfOutlined
} from '@ant-design/icons'
import type { Devis, QuoteItem } from '../types/api'

const { Title, Text } = Typography
const { TextArea } = Input

const STATUS_COLOR: Record<string, string> = {
  'Brouillon':   'default',
  'À compléter': 'orange',
  'À valider':   'blue',
  'Prêt':        'green',
  'Rejeté':      'red',
}

export default function DevisEditorPage() {
  const { quoteNumber } = useParams<{ quoteNumber: string }>()
  const navigate = useNavigate()

  const [devis, setDevis]               = useState<Devis | null>(null)
  const [loading, setLoading]           = useState(true)
  const [items, setItems]               = useState<QuoteItem[]>([])
  const [remise, setRemise]             = useState<number>(0)
  const [commentaire, setCommentaire]   = useState('')
  const [conditions, setConditions]     = useState('')
  const [saving, setSaving]             = useState(false)
  const [rejectModal, setRejectModal]   = useState(false)
  const [clientNom, setClientNom]       = useState<string>('')
  const [priorite, setPriorite]         = useState<string>('')
  const [budgetClient, setBudgetClient] = useState<number | null>(null)
  const [sujetBesoin, setSujetBesoin]   = useState<string>('')
  const [deliveryDate, setDeliveryDate] = useState<dayjs.Dayjs | null>(null)

  useEffect(() => {
    fetch(`/api/devis/${quoteNumber}`)
      .then(r => r.json())
      .then((data: Devis) => {
        setDevis(data)
        setItems(data.items ?? [])
        setClientNom(data.clientNom ?? '')
        setPriorite(data.priority ? data.priority.toUpperCase() : 'NORMALE')
        setBudgetClient(data.clientBudget ?? null)
        setSujetBesoin(data.subject ?? '')
        setDeliveryDate(data.requestedDeliveryDate ? dayjs(data.requestedDeliveryDate) : null)
      })
      .catch(() => message.error('Impossible de charger le devis'))
      .finally(() => setLoading(false))
  }, [quoteNumber])

  // Recalcul des totaux en temps réel
  const totalHT = items.reduce((sum, item) => {
    const base = (item.unitPriceHT ?? 0) * item.quantity
    const apresRemise = base * (1 - (item.discountPercent ?? 0) / 100)
    return sum + apresRemise
  }, 0)
  const apresRemiseGlobale = totalHT * (1 - remise / 100)
  const tvaRate = devis?.tvaRate ?? 20
  const totalTVA = apresRemiseGlobale * (tvaRate / 100)
  const totalTTC = apresRemiseGlobale + totalTVA

  const updateItem = (index: number, field: keyof QuoteItem, value: number) => {
    setItems(prev => {
      const next = [...prev]
      next[index] = { ...next[index], [field]: value }
      return next
    })
  }

  const removeItem = (index: number) => {
    setItems(prev => prev.filter((_, i) => i !== index))
  }

  const addItem = () => {
    setItems(prev => [...prev, {
      lineNumber: prev.length + 1,
      designation: 'Nouvel article',
      quantity: 1,
      unitPriceHT: 0,
      discountPercent: 0,
      totalPriceHT: 0,
      category: 'AUTRE',
      status: 'PRICING_OK',
    }])
  }

  const sauvegarder = async () => {
    setSaving(true)
    try {
      const res = await fetch(`/api/devis/${quoteNumber}/valider`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          nomClient: clientNom.trim() || null,
          priorite: priorite || null,
          budgetClient: budgetClient,
          sujetBesoin: sujetBesoin.trim() || null,
          dateLivraison: deliveryDate ? deliveryDate.format('YYYY-MM-DD') : null,
          items: items.map((it, idx) => ({
            lineNumber:      it.lineNumber ?? idx + 1,
            designation:     it.designation,
            quantity:        it.quantity,
            unitPriceHT:     it.unitPriceHT ?? 0,
            discountPercent: it.discountPercent ?? 0,
          })),
        }),
      })
      if (!res.ok) { message.error('Erreur lors de la sauvegarde'); return }
      const updated = await res.json()
      setDevis(updated)
      message.success('Modifications sauvegardées')
    } catch {
      message.error('Erreur réseau')
    } finally {
      setSaving(false)
    }
  }

  const valider = async (statut: 'PRET' | 'REJETE') => {
    setSaving(true)
    try {
      const res = await fetch(`/api/devis/${quoteNumber}/valider`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          statut,
          commentaire,
          remiseGlobale: remise > 0 ? remise : null,
          conditionsPaiement: conditions || null,
          nomClient: clientNom.trim() || null,
          priorite: priorite || null,
          budgetClient: budgetClient,
          sujetBesoin: sujetBesoin.trim() || null,
          dateLivraison: deliveryDate ? deliveryDate.format('YYYY-MM-DD') : null,
          items: items.map((it, idx) => ({
            lineNumber:      it.lineNumber ?? idx + 1,
            designation:     it.designation,
            quantity:        it.quantity,
            unitPriceHT:     it.unitPriceHT ?? 0,
            discountPercent: it.discountPercent ?? 0,
          })),
        }),
      })
      if (!res.ok) {
        message.error('Erreur lors de la mise à jour du devis')
        return
      }
      message.success(statut === 'PRET' ? 'Devis validé !' : 'Devis rejeté')
      navigate('/')
    } catch {
      message.error('Erreur réseau')
    } finally {
      setSaving(false)
      setRejectModal(false)
    }
  }

  if (loading) return <Spin size="large" style={{ display: 'block', marginTop: 80, textAlign: 'center' }} />
  if (!devis)  return <Alert type="error" message="Devis introuvable" style={{ margin: 24 }} />

  const confidence = devis.confidence <= 1
    ? Math.round(devis.confidence * 100)
    : Math.round(devis.confidence)

  const columns = [
    {
      title: 'Désignation',
      dataIndex: 'designation',
      key: 'designation',
      render: (v: string, _: QuoteItem, index: number) => (
        <Input
          value={v}
          onChange={e => {
            setItems(prev => {
              const next = [...prev]
              next[index] = { ...next[index], designation: e.target.value }
              return next
            })
          }}
          style={{ minWidth: 160 }}
        />
      ),
    },
    {
      title: 'Catégorie',
      dataIndex: 'category',
      key: 'category',
      render: (v: string) => <Tag>{v}</Tag>,
    },
    {
      title: 'Qté',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 90,
      render: (v: number, _: QuoteItem, index: number) => (
        <InputNumber
          min={1}
          value={v}
          onChange={val => updateItem(index, 'quantity', val ?? 1)}
          style={{ width: 70 }}
        />
      ),
    },
    {
      title: 'Prix unitaire HT',
      dataIndex: 'unitPriceHT',
      key: 'unitPriceHT',
      width: 140,
      render: (v: number | null, _: QuoteItem, index: number) => (
        <InputNumber
          min={0}
          value={v ?? 0}
          onChange={val => updateItem(index, 'unitPriceHT', val ?? 0)}
          addonAfter="€"
          style={{ width: 120 }}
        />
      ),
    },
    {
      title: 'Remise %',
      dataIndex: 'discountPercent',
      key: 'discountPercent',
      width: 100,
      render: (v: number | null, _: QuoteItem, index: number) => (
        <InputNumber
          min={0}
          max={100}
          value={v ?? 0}
          onChange={val => updateItem(index, 'discountPercent', val ?? 0)}
          addonAfter="%"
          style={{ width: 85 }}
        />
      ),
    },
    {
      title: 'Total HT',
      key: 'total',
      width: 120,
      render: (_: unknown, record: QuoteItem) => {
        const base = (record.unitPriceHT ?? 0) * record.quantity
        const total = base * (1 - (record.discountPercent ?? 0) / 100)
        return <Text strong>{total.toLocaleString('fr-FR', { minimumFractionDigits: 2 })} €</Text>
      },
    },
    {
      title: '',
      key: 'action',
      width: 50,
      render: (_: unknown, __: QuoteItem, index: number) => (
        <Button danger size="small" onClick={() => removeItem(index)}>✕</Button>
      ),
    },
  ]

  const hasAlerts = (devis.warnings?.length ?? 0) > 0
    || (devis.requiredActions?.length ?? 0) > 0
    || (devis.inconsistencies?.length ?? 0) > 0

  const estExpire = devis.validUntil
    ? new Date(devis.validUntil) < new Date()
    : false

  return (
    <div style={{ padding: 24, background: '#f5f5f5', minHeight: '100vh' }}>

      {/* Avertissement expiration */}
      {estExpire && (
        <Alert
          type="error"
          showIcon
          message="Devis expiré"
          description={`Ce devis a expiré le ${new Date(devis.validUntil!).toLocaleDateString('fr-FR')} — il ne peut plus être envoyé au client. Vous pouvez le dupliquer ou le revalider.`}
          style={{ marginBottom: 16 }}
        />
      )}

      {/* En-tête */}
      <Space style={{ marginBottom: 20 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/')}>
          Retour à l'inbox
        </Button>
        <Title level={4} style={{ margin: 0 }}>
          {quoteNumber} — <Tag color={STATUS_COLOR[devis.status] ?? 'default'}>{devis.status}</Tag>
        </Title>
      </Space>

      <Row gutter={24}>

        {/* Colonne principale */}
        <Col xs={24} lg={hasAlerts ? 17 : 24}>

          {/* Infos client */}
          <Card style={{ marginBottom: 16 }}>
            <Descriptions title="Informations client" column={2} size="small">
              <Descriptions.Item label="Client">
                <Input
                  value={clientNom}
                  onChange={e => setClientNom(e.target.value)}
                  placeholder="Non détecté — saisir manuellement"
                  style={{ width: 220 }}
                />
              </Descriptions.Item>
              <Descriptions.Item label="Email">
                {devis.clientEmail
                  ? <a href={`mailto:${devis.clientEmail}`}>{devis.clientEmail}</a>
                  : <Text type="secondary">Non détecté</Text>}
              </Descriptions.Item>
              <Descriptions.Item label="Priorité">
                <Select
                  value={priorite || 'Normale'}
                  onChange={val => setPriorite(val)}
                  style={{ width: 140 }}
                  options={[
                    { value: 'BASSE',    label: 'Basse' },
                    { value: 'NORMALE',  label: 'Normale' },
                    { value: 'HAUTE',    label: 'Haute' },
                    { value: 'URGENTE',  label: 'Urgente' },
                  ]}
                />
              </Descriptions.Item>
              <Descriptions.Item label="Aperçu du besoin" span={2}>
                <Input
                  value={sujetBesoin}
                  onChange={e => setSujetBesoin(e.target.value)}
                  placeholder="Décrire le besoin du client"
                  style={{ width: '100%' }}
                />
              </Descriptions.Item>
              <Descriptions.Item label="Date de création">
                {devis.createdAt ? new Date(devis.createdAt).toLocaleDateString('fr-FR') : '—'}
              </Descriptions.Item>
              <Descriptions.Item label="Valide jusqu'au">
                {devis.validUntil ?? '—'}
              </Descriptions.Item>
              <Descriptions.Item label="Confiance IA">
                <Tag color={confidence >= 75 ? 'green' : confidence >= 50 ? 'orange' : 'red'}>
                  {confidence}%
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Date de livraison souhaitée">
                <DatePicker
                  value={deliveryDate}
                  onChange={date => setDeliveryDate(date)}
                  format="DD/MM/YYYY"
                  placeholder="Non précisée"
                />
              </Descriptions.Item>
              <Descriptions.Item label="Budget client">
                <InputNumber
                  value={budgetClient}
                  onChange={val => setBudgetClient(val)}
                  placeholder="Non précisé"
                  min={0}
                  addonAfter="€"
                  style={{ width: 160 }}
                />
              </Descriptions.Item>
            </Descriptions>
          </Card>

          {/* Tableau des articles */}
          <Card
            title="Articles du devis"
            extra={<Button type="dashed" onClick={addItem}>+ Ajouter un article</Button>}
            style={{ marginBottom: 16 }}
          >
            <Table
              columns={columns}
              dataSource={items}
              rowKey={(_, i) => String(i)}
              pagination={false}
              size="small"
            />
          </Card>

          {/* Remise globale + conditions */}
          <Card title="Conditions commerciales" style={{ marginBottom: 16 }}>
            <Row gutter={16}>
              <Col span={8}>
                <Text>Remise globale</Text>
                <InputNumber
                  min={0} max={100}
                  value={remise}
                  onChange={val => setRemise(val ?? 0)}
                  addonAfter="%"
                  style={{ width: '100%', marginTop: 4 }}
                />
              </Col>
              <Col span={16}>
                <Text>Conditions de paiement</Text>
                <Input
                  placeholder="Ex : 30 jours net, virement bancaire"
                  value={conditions}
                  onChange={e => setConditions(e.target.value)}
                  style={{ marginTop: 4 }}
                />
              </Col>
            </Row>
            <Row style={{ marginTop: 12 }}>
              <Col span={24}>
                <Text>Commentaire</Text>
                <TextArea
                  rows={2}
                  placeholder="Notes internes ou pour le client..."
                  value={commentaire}
                  onChange={e => setCommentaire(e.target.value)}
                  style={{ marginTop: 4 }}
                />
              </Col>
            </Row>
          </Card>

          {/* Récapitulatif financier */}
          <Card style={{ marginBottom: 16 }}>
            <Row gutter={16} justify="end">
              <Col>
                <Statistic title="Total HT" value={totalHT} precision={2} suffix="€" />
              </Col>
              {remise > 0 && (
                <Col>
                  <Statistic
                    title={`Remise (${remise}%)`}
                    value={-(totalHT * remise / 100)}
                    precision={2}
                    suffix="€"
                    valueStyle={{ color: '#cf1322' }}
                  />
                </Col>
              )}
              <Col>
                <Statistic title={`TVA (${tvaRate}%)`} value={totalTVA} precision={2} suffix="€" />
              </Col>
              <Col>
                <Statistic
                  title="Total TTC"
                  value={totalTTC}
                  precision={2}
                  suffix="€"
                  valueStyle={{ color: '#3f8600', fontSize: 24 }}
                />
              </Col>
            </Row>
          </Card>

          {/* Email original */}
          {devis.emailOriginal && (
            <Collapse
              style={{ marginBottom: 16 }}
              items={[{
                key: '1',
                label: <Space><MailOutlined /> Email original du client</Space>,
                children: (
                  <pre style={{
                    whiteSpace: 'pre-wrap',
                    fontFamily: 'inherit',
                    margin: 0,
                    background: '#fafafa',
                    padding: 12,
                    borderRadius: 6,
                    fontSize: 13,
                    lineHeight: 1.6,
                  }}>
                    {devis.emailOriginal}
                  </pre>
                ),
              }]}
            />
          )}

          {/* Boutons d'action */}
          <Space size="middle">
            <Button
              size="large"
              loading={saving}
              onClick={sauvegarder}
            >
              Sauvegarder les modifications
            </Button>
            <Button
              type="primary"
              size="large"
              icon={<CheckOutlined />}
              loading={saving}
              disabled={devis.status === 'Prêt' || devis.status === 'Rejeté'}
              onClick={() => valider('PRET')}
            >
              {devis.status === 'Prêt' ? 'Déjà validé' : 'Valider le devis'}
            </Button>
            <Button
              danger
              size="large"
              icon={<CloseOutlined />}
              disabled={devis.status === 'Rejeté'}
              onClick={() => setRejectModal(true)}
            >
              {devis.status === 'Rejeté' ? 'Devis rejeté' : 'Rejeter'}
            </Button>
            <Button
              size="large"
              icon={<FilePdfOutlined />}
              onClick={() => window.open(`/api/devis/${quoteNumber}/pdf`, '_blank')}
            >
              Télécharger le PDF
            </Button>
          </Space>

        </Col>

        {/* Colonne alertes (côté droit) */}
        {hasAlerts && (
          <Col xs={24} lg={7}>

            {(devis.requiredActions?.length ?? 0) > 0 && (
              <Alert
                type="warning"
                icon={<WarningOutlined />}
                message="Actions requises"
                description={
                  <List
                    size="small"
                    dataSource={devis.requiredActions}
                    renderItem={item => <List.Item>• {item}</List.Item>}
                  />
                }
                style={{ marginBottom: 12 }}
              />
            )}

            {(devis.warnings?.length ?? 0) > 0 && (
              <Alert
                type="warning"
                message="Avertissements"
                description={
                  <List
                    size="small"
                    dataSource={devis.warnings}
                    renderItem={item => <List.Item>• {item}</List.Item>}
                  />
                }
                style={{ marginBottom: 12 }}
              />
            )}

            {(devis.inconsistencies?.length ?? 0) > 0 && (
              <Alert
                type="error"
                message="Incohérences détectées"
                description={
                  <List
                    size="small"
                    dataSource={devis.inconsistencies}
                    renderItem={item => <List.Item>• {item}</List.Item>}
                  />
                }
                style={{ marginBottom: 12 }}
              />
            )}

            {(devis.recommendations?.length ?? 0) > 0 && (
              <Alert
                type="info"
                icon={<InfoCircleOutlined />}
                message="Recommandations IA"
                description={
                  <List
                    size="small"
                    dataSource={devis.recommendations}
                    renderItem={item => <List.Item>• {item}</List.Item>}
                  />
                }
              />
            )}

          </Col>
        )}
      </Row>

      {/* Modal confirmation rejet */}
      <Modal
        title="Rejeter le devis"
        open={rejectModal}
        onOk={() => valider('REJETE')}
        onCancel={() => setRejectModal(false)}
        okText="Confirmer le rejet"
        okButtonProps={{ danger: true }}
        confirmLoading={saving}
      >
        <Text>Es-tu sûr de vouloir rejeter ce devis ? Ajoute un commentaire si nécessaire.</Text>
        <TextArea
          rows={3}
          placeholder="Motif du rejet..."
          value={commentaire}
          onChange={e => setCommentaire(e.target.value)}
          style={{ marginTop: 12 }}
        />
      </Modal>

    </div>
  )
}
