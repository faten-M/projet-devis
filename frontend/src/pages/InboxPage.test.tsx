import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import InboxPage from './InboxPage'
import type { Devis } from '../types/api'

const devisFixture: Devis[] = [
  {
    quoteNumber: 'DEV-2026-001',
    createdAt: '2026-05-01T10:00:00Z',
    validUntil: '2026-07-01',
    status: 'À valider',
    priority: 'NORMAL',
    subject: 'Commande ciment BTP',
    clientNom: 'Martin Travaux',
    clientEmail: 'martin@btp.fr',
    emailOriginal: null,
    totalHT: 1500,
    totalTTC: 1800,
    tvaRate: 20,
    clientBudget: null,
    confidence: 85,
    items: [],
    requiredActions: [],
    recommendations: [],
    warnings: [],
    inconsistencies: [],
  },
]

function renderInbox() {
  return render(
    <MemoryRouter>
      <InboxPage />
    </MemoryRouter>
  )
}

describe('InboxPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  it('affiche les devis après chargement', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => devisFixture,
    } as Response)

    renderInbox()

    await waitFor(() => {
      expect(screen.getByText('DEV-2026-001')).toBeInTheDocument()
      expect(screen.getByText('Martin Travaux')).toBeInTheDocument()
      expect(screen.getByText('À valider')).toBeInTheDocument()
    })
  })

  it('affiche "Aucun devis" quand la liste est vide', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    } as Response)

    renderInbox()

    await waitFor(() => {
      expect(screen.getByText('Aucun devis pour le moment.')).toBeInTheDocument()
    })
  })

  it('bloque la soumission si le champ email est vide (pas de POST)', async () => {
    const user = userEvent.setup()

    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => [],
    } as Response)

    renderInbox()
    await waitFor(() => screen.getByText('+ Nouvelle demande'))

    await user.click(screen.getByText('+ Nouvelle demande'))
    await user.click(screen.getByText('Générer le devis'))

    // Seul le GET initial doit avoir été appelé, pas de POST
    expect(fetch).toHaveBeenCalledTimes(1)
    expect(vi.mocked(fetch).mock.calls[0][0]).toBe('/api/devis')
    expect((vi.mocked(fetch).mock.calls[0][1] as RequestInit | undefined)?.method).toBeUndefined()
  })
})
