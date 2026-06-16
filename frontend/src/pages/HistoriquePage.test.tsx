import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import HistoriquePage from './HistoriquePage'
import type { Devis, Client } from '../types/api'

const devisFixtures: Devis[] = [
  {
    quoteNumber: 'DEV-2026-001',
    createdAt: '2026-05-01T10:00:00Z',
    validUntil: '2026-07-01',
    status: 'Prêt',
    priority: 'NORMAL',
    subject: 'Commande ciment',
    clientNom: 'Martin Travaux',
    clientEmail: 'martin@btp.fr',
    emailOriginal: null,
    totalHT: 1500,
    totalTTC: 1800,
    tvaRate: 20,
    clientBudget: null,
    requestedDeliveryDate: null,
    deliveryFees: null,
    deliveryIncluded: false,
    warranty: null,
    confidence: 85,
    items: [],
    requiredActions: [],
    recommendations: [],
    warnings: [],
    inconsistencies: [],
  },
  {
    quoteNumber: 'DEV-2026-002',
    createdAt: '2026-05-15T10:00:00Z',
    validUntil: '2026-07-15',
    status: 'À valider',
    priority: 'URGENT',
    subject: 'Ferrailles pour chantier',
    clientNom: 'Dupont BTP',
    clientEmail: 'dupont@btp.fr',
    emailOriginal: null,
    totalHT: 800,
    totalTTC: 960,
    tvaRate: 20,
    clientBudget: null,
    requestedDeliveryDate: null,
    deliveryFees: null,
    deliveryIncluded: false,
    warranty: null,
    confidence: 60,
    items: [],
    requiredActions: [],
    recommendations: [],
    warnings: [],
    inconsistencies: [],
  },
]

const clientFixtures: Client[] = [
  {
    clientId: 'c1',
    raisonSociale: 'Martin Travaux',
    emailOrigine: 'martin@btp.fr',
    segment: 'PME',
    status: 'Actif',
    nombreCommandes: 3,
    historiqueDevis: ['DEV-2026-001'],
  },
]

function renderHistorique() {
  return render(
    <MemoryRouter>
      <HistoriquePage />
    </MemoryRouter>
  )
}

describe('HistoriquePage — onglet Historique', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
    // mockResolvedValue (sans Once) = réponse par défaut pour tous les appels
    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: async () => devisFixtures,
    } as Response)
  })

  it('affiche tous les devis', async () => {
    renderHistorique()

    await waitFor(() => {
      expect(screen.getByText('DEV-2026-001')).toBeInTheDocument()
      expect(screen.getByText('Martin Travaux')).toBeInTheDocument()
      expect(screen.getByText('DEV-2026-002')).toBeInTheDocument()
      expect(screen.getByText('Dupont BTP')).toBeInTheDocument()
    })
  })

  it('filtre les devis par nom de client', async () => {
    const user = userEvent.setup()
    renderHistorique()

    await waitFor(() => screen.getByText('DEV-2026-001'))

    await user.type(
      screen.getByPlaceholderText('Rechercher par client ou n° devis'),
      'Martin'
    )

    await waitFor(() => {
      expect(screen.getByText('DEV-2026-001')).toBeInTheDocument()
      expect(screen.queryByText('DEV-2026-002')).not.toBeInTheDocument()
    })
  })

  it('filtre les devis par numéro de devis', async () => {
    const user = userEvent.setup()
    renderHistorique()

    await waitFor(() => screen.getByText('DEV-2026-002'))

    await user.type(
      screen.getByPlaceholderText('Rechercher par client ou n° devis'),
      'DEV-2026-002'
    )

    await waitFor(() => {
      expect(screen.queryByText('DEV-2026-001')).not.toBeInTheDocument()
      expect(screen.getByText('DEV-2026-002')).toBeInTheDocument()
    })
  })

  it('affiche "0 devis" si aucun résultat ne correspond au filtre', async () => {
    const user = userEvent.setup()
    renderHistorique()

    await waitFor(() => screen.getByText('DEV-2026-001'))

    await user.type(
      screen.getByPlaceholderText('Rechercher par client ou n° devis'),
      'client inexistant'
    )

    await waitFor(() => {
      expect(screen.getByText('0 devis')).toBeInTheDocument()
    })
  })
})

describe('HistoriquePage — onglet Clients', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  it('affiche les clients avec leur segment et statut après clic sur l\'onglet', async () => {
    // Les deux mocks sont prêts avant le rendu :
    // antd Tabs peut pré-rendre les deux onglets ou attendre le clic — les deux cas sont couverts
    vi.mocked(fetch)
      .mockResolvedValueOnce({ ok: true, json: async () => devisFixtures } as Response)  // GET /api/devis
      .mockResolvedValueOnce({ ok: true, json: async () => clientFixtures } as Response) // GET /api/clients

    const user = userEvent.setup()
    renderHistorique()

    await user.click(screen.getByRole('tab', { name: 'Clients' }))

    await waitFor(() => {
      expect(screen.getByText('PME')).toBeInTheDocument()
      expect(screen.getByText('Actif')).toBeInTheDocument()
    })
  })
})
