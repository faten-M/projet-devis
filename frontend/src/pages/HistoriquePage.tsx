import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Tabs, Table, Tag, Input, Select, Typography,
  message, Space, Badge, Descriptions, Spin, DatePicker
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import type { Devis, Client } from '../types/api'
import type { Dayjs } from 'dayjs'

const { RangePicker } = DatePicker

const { Title, Text } = Typography
const { Search } = Input

const STATUS_COLOR: Record<string, string> = {
  'Brouillon':   'default',
  'À compléter': 'orange',
  'À valider':   'blue',
  'Prêt':        'green',
  'Rejeté':      'red',
}

// ─── Onglet Historique ────────────────────────────────────────────────────────

function HistoriqueTab() {
  const navigate = useNavigate()
  const [devis, setDevis]           = useState<Devis[]>([])
  const [filtered, setFiltered]     = useState<Devis[]>([])
  const [loading, setLoading]       = useState(true)
  const [searchClient, setSearch]   = useState('')
  const [filterStatus, setStatus]   = useState<string | null>(null)
  const [dateRange, setDateRange]   = useState<[Dayjs, Dayjs] | null>(null)

  useEffect(() => {
    fetch('/api/devis')
      .then(r => r.json())
      .then((data: Devis[]) => { setDevis(data); setFiltered(data) })
      .catch(() => message.error('Impossible de charger les devis'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    let result = devis
    if (searchClient.trim()) {
      result = result.filter(d =>
        (d.clientNom ?? '').toLowerCase().includes(searchClient.toLowerCase()) ||
        d.quoteNumber.toLowerCase().includes(searchClient.toLowerCase())
      )
    }
    if (filterStatus) {
      result = result.filter(d => d.status === filterStatus)
    }
    if (dateRange) {
      const [start, end] = dateRange
      result = result.filter(d => {
        if (!d.createdAt) return false
        const date = new Date(d.createdAt).getTime()
        return date >= start.startOf('day').valueOf() && date <= end.endOf('day').valueOf()
      })
    }
    setFiltered(result)
  }, [searchClient, filterStatus, dateRange, devis])

  const columns: ColumnsType<Devis> = [
    {
      title: 'N° Devis',
      dataIndex: 'quoteNumber',
      key: 'quoteNumber',
      render: (v: string) => <Text strong style={{ fontFamily: 'monospace' }}>{v}</Text>,
      sorter: (a, b) => a.quoteNumber.localeCompare(b.quoteNumber),
    },
    {
      title: 'Date',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (v: string) => v ? new Date(v).toLocaleDateString('fr-FR') : '—',
      sorter: (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
      defaultSortOrder: 'descend',
    },
    {
      title: 'Client',
      dataIndex: 'clientNom',
      key: 'clientNom',
      render: (v: string | null) => v || <Text type="secondary">—</Text>,
    },
    {
      title: 'Aperçu du besoin',
      dataIndex: 'subject',
      key: 'subject',
      ellipsis: true,
      render: (v: string) => v || '—',
    },
    {
      title: 'Montant HT',
      dataIndex: 'totalHT',
      key: 'totalHT',
      render: (v: number | null) =>
        v != null ? `${v.toLocaleString('fr-FR', { minimumFractionDigits: 2 })} €` : '—',
      align: 'right',
      sorter: (a, b) => (a.totalHT ?? 0) - (b.totalHT ?? 0),
    },
    {
      title: 'Statut',
      dataIndex: 'status',
      key: 'status',
      render: (v: string) => <Tag color={STATUS_COLOR[v] ?? 'default'}>{v}</Tag>,
    },
    {
      title: 'Confiance IA',
      dataIndex: 'confidence',
      key: 'confidence',
      render: (v: number) => {
        const pct = v <= 1 ? Math.round(v * 100) : Math.round(v)
        const color = pct >= 75 ? 'green' : pct >= 50 ? 'orange' : 'red'
        return <Badge color={color} text={`${pct}%`} />
      },
      align: 'center',
    },
  ]

  return (
    <div>
      <Space style={{ marginBottom: 16, flexWrap: 'wrap' }}>
        <Search
          placeholder="Rechercher par client ou n° devis"
          allowClear
          style={{ width: 260 }}
          onChange={e => setSearch(e.target.value)}
        />
        <RangePicker
          placeholder={['Date début', 'Date fin']}
          format="DD/MM/YYYY"
          onChange={v => setDateRange(v as [Dayjs, Dayjs] | null)}
        />
        <Select
          placeholder="Filtrer par statut"
          allowClear
          style={{ width: 180 }}
          onChange={v => setStatus(v ?? null)}
          options={Object.keys(STATUS_COLOR).map(s => ({ label: s, value: s }))}
        />
        <Text type="secondary">{filtered.length} devis</Text>
      </Space>

      <Table<Devis>
        columns={columns}
        dataSource={filtered}
        rowKey="quoteNumber"
        loading={loading}
        pagination={{ pageSize: 10 }}
        locale={{ emptyText: 'Aucun devis trouvé.' }}
        onRow={record => ({
          onClick: () => navigate(`/devis/${record.quoteNumber}`),
          style: { cursor: 'pointer' },
        })}
      />
    </div>
  )
}

// ─── Onglet Clients ───────────────────────────────────────────────────────────

function ClientsTab() {
  const navigate = useNavigate()
  const [clients, setClients] = useState<Client[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetch('/api/clients')
      .then(r => r.json())
      .then((data: Client[]) => setClients(data))
      .catch(() => message.error('Impossible de charger les clients'))
      .finally(() => setLoading(false))
  }, [])

  const columns: ColumnsType<Client> = [
    {
      title: 'Client',
      dataIndex: 'raisonSociale',
      key: 'raisonSociale',
      render: (v: string) => <Text strong>{v}</Text>,
      sorter: (a, b) => a.raisonSociale.localeCompare(b.raisonSociale),
    },
    {
      title: 'Email',
      dataIndex: 'emailOrigine',
      key: 'emailOrigine',
      render: (v: string | null) => v || <Text type="secondary">—</Text>,
    },
    {
      title: 'Segment',
      dataIndex: 'segment',
      key: 'segment',
      render: (v: string | null) => v ? <Tag>{v}</Tag> : '—',
    },
    {
      title: 'Statut',
      dataIndex: 'status',
      key: 'status',
      render: (v: string | null) => v
        ? <Tag color={v === 'Actif' ? 'green' : v === 'Bloqué' ? 'red' : 'default'}>{v}</Tag>
        : '—',
    },
    {
      title: 'Nb devis',
      dataIndex: 'historiqueDevis',
      key: 'nbDevis',
      render: (v: string[]) => v?.length ?? 0,
      align: 'center',
      sorter: (a, b) => (a.historiqueDevis?.length ?? 0) - (b.historiqueDevis?.length ?? 0),
    },
  ]

  const expandedRow = (client: Client) => (
    <Descriptions size="small" column={1} style={{ padding: '8px 16px' }}>
      <Descriptions.Item label="Devis associés">
        <Space wrap>
          {client.historiqueDevis?.length > 0
            ? client.historiqueDevis.map(qn => (
                <Tag
                  key={qn}
                  style={{ cursor: 'pointer', fontFamily: 'monospace' }}
                  color="blue"
                  onClick={() => navigate(`/devis/${qn}`)}
                >
                  {qn}
                </Tag>
              ))
            : <Text type="secondary">Aucun devis</Text>
          }
        </Space>
      </Descriptions.Item>
    </Descriptions>
  )

  if (loading) return <Spin style={{ display: 'block', marginTop: 40, textAlign: 'center' }} />

  return (
    <Table<Client>
      columns={columns}
      dataSource={clients}
      rowKey="clientId"
      pagination={{ pageSize: 10 }}
      locale={{ emptyText: 'Aucun client enregistré.' }}
      expandable={{ expandedRowRender: expandedRow }}
    />
  )
}

// ─── Page principale ──────────────────────────────────────────────────────────

export default function HistoriquePage() {
  return (
    <div style={{ padding: 24 }}>
      <Title level={3} style={{ marginBottom: 24 }}>Historique & Clients</Title>
      <Tabs
        defaultActiveKey="historique"
        items={[
          { key: 'historique', label: 'Historique des devis', children: <HistoriqueTab /> },
          { key: 'clients',    label: 'Clients',              children: <ClientsTab /> },
        ]}
      />
    </div>
  )
}
