import { ConfigProvider, theme, Layout, Menu } from 'antd'
import frFR from 'antd/locale/fr_FR'
import { BrowserRouter, Routes, Route, useNavigate, useLocation } from 'react-router-dom'
import InboxPage from './pages/InboxPage'
import DevisEditorPage from './pages/DevisEditorPage'
import HistoriquePage from './pages/HistoriquePage'

const { Header, Content } = Layout

function AppLayout() {
  const navigate = useNavigate()
  const location = useLocation()

  const currentKey = location.pathname.startsWith('/historique')
    ? 'historique'
    : 'inbox'

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', alignItems: 'center', gap: 24 }}>
        <span style={{ color: 'white', fontWeight: 700, fontSize: 16, marginRight: 16 }}>
          DevisAI
        </span>
        <Menu
          theme="dark"
          mode="horizontal"
          selectedKeys={[currentKey]}
          style={{ flex: 1, background: 'transparent', borderBottom: 'none' }}
          items={[
            { key: 'inbox',      label: 'Inbox',              onClick: () => navigate('/') },
            { key: 'historique', label: 'Historique & Clients', onClick: () => navigate('/historique') },
          ]}
        />
      </Header>
      <Content style={{ background: '#f5f5f5' }}>
        <Routes>
          <Route path="/"                      element={<InboxPage />} />
          <Route path="/devis/:quoteNumber"    element={<DevisEditorPage />} />
          <Route path="/historique"            element={<HistoriquePage />} />
        </Routes>
      </Content>
    </Layout>
  )
}

export default function App() {
  return (
    <ConfigProvider locale={frFR} theme={{ algorithm: theme.defaultAlgorithm }}>
      <BrowserRouter>
        <AppLayout />
      </BrowserRouter>
    </ConfigProvider>
  )
}
