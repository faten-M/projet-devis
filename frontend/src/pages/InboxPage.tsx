import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Table, Tag, Button, Modal, Input, Typography,
  Space, message, Badge, Tooltip
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import type { Devis } from '../types/api'

const { TextArea } = Input
const { Title, Text } = Typography

const STATUS_COLOR: Record<string, string> = {
  'Brouillon':    'default',
  'À compléter':  'orange',
  'À valider':    'blue',
  'Prêt':         'green',
  'Rejeté':       'red',
}

export default function InboxPage() {
  const navigate = useNavigate()
  const [devis, setDevis]           = useState<Devis[]>([])
  const [loading, setLoading]       = useState(true)
  const [modalOpen, setModalOpen]   = useState(false)
  const [emailText, setEmailText]   = useState('')
  const [sending, setSending]       = useState(false)

  const fetchDevis = () => {
    setLoading(true)
    fetch('/api/devis')
      .then(r => r.json())
      .then((data: Devis[]) => setDevis(data))
      .catch(() => message.error('Impossible de charger les devis'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchDevis() }, [])

  const handleNouvelleDemanande = async () => {
    if (!emailText.trim()) {
      message.warning('Collez un email avant de valider.')
      return
    }
    setSending(true)
    try {
      const res = await fetch('/api/devis', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ emailText }),
      })
      if (!res.ok) {
        const err = await res.text()
        message.error(`Erreur : ${err}`)
        return
      }
      message.success('Devis généré avec succès !')
      setModalOpen(false)
      setEmailText('')
      fetchDevis()
    } catch {
      message.error('Erreur réseau.')
    } finally {
      setSending(false)
    }
  }

  const columns: ColumnsType<Devis> = [
    {
      title: 'N° Devis',
      dataIndex: 'quoteNumber',
      key: 'quoteNumber',
      render: (v: string) => <Text strong style={{ fontFamily: 'monospace' }}>{v}</Text>,
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
      render: (v: string | null) => v || <Text type="secondary">Non détecté</Text>,
    },
    {
      title: 'Aperçu du besoin',
      dataIndex: 'subject',
      key: 'subject',
      render: (v: string) => v
        ? <Tooltip title={v}><span>{v}</span></Tooltip>
        : '—',
      ellipsis: true,
    },
    {
      title: 'Montant HT',
      dataIndex: 'totalHT',
      key: 'totalHT',
      render: (v: number | null) =>
        v != null ? `${v.toLocaleString('fr-FR', { minimumFractionDigits: 2 })} €` : '—',
      align: 'right',
    },
    {
      title: 'Statut',
      dataIndex: 'status',
      key: 'status',
      render: (v: string) => <Tag color={STATUS_COLOR[v] ?? 'default'}>{v}</Tag>,
      filters: Object.keys(STATUS_COLOR).map(s => ({ text: s, value: s })),
      onFilter: (value, record) => record.status === value,
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
    <div style={{ padding: '24px' }}>
      <Space style={{ marginBottom: 20, width: '100%', justifyContent: 'space-between' }} align="center">
        <Title level={3} style={{ margin: 0 }}>Inbox — Demandes de devis</Title>
        <Button type="primary" size="large" onClick={() => setModalOpen(true)}>
          + Nouvelle demande
        </Button>
      </Space>

      <Table<Devis>
        columns={columns}
        dataSource={devis}
        rowKey="quoteNumber"
        loading={loading}
        pagination={{ pageSize: 10 }}
        locale={{ emptyText: 'Aucun devis pour le moment.' }}
        onRow={(record) => ({
          onClick: () => navigate(`/devis/${record.quoteNumber}`),
          style: { cursor: 'pointer' },
        })}
      />

      <Modal
        title="Nouvelle demande — Coller un email"
        open={modalOpen}
        onOk={handleNouvelleDemanande}
        onCancel={() => { setModalOpen(false); setEmailText('') }}
        okText="Générer le devis"
        cancelText="Annuler"
        confirmLoading={sending}
        width={640}
      >
        <Text type="secondary" style={{ display: 'block', marginBottom: 12 }}>
          Collez ici le texte de l'email client. L'IA va extraire les informations et générer un devis.
        </Text>
        <TextArea
          rows={10}
          placeholder="Bonjour, je souhaite commander 50 sacs de ciment CEM II..."
          value={emailText}
          onChange={e => setEmailText(e.target.value)}
        />
      </Modal>
    </div>
  )
}
