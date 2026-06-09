import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Routes, Route } from 'react-router-dom'
import DevisEditorPage from './DevisEditorPage'
import type { Devis } from '../types/api'

const devisFixture: Devis = {
  quoteNumber: 'DEV-2026-001',
  createdAt: '2026-05-01T10:00:00Z',
  validUntil: '2030-01-01',
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
  requestedDeliveryDate: null,
  confidence: 85,
  items: [
    {
      lineNumber: 1,
      designation: 'Ciment CEM II',
      quantity: 10,
      unitPriceHT: 50,
      discountPercent: 0,
      totalPriceHT: 500,
      category: 'MATERIAUX',
      status: 'PRICING_OK',
    },
  ],
  requiredActions: [],
  recommendations: [],
  warnings: [],
  inconsistencies: [],
}

function renderEditor(quoteNumber = 'DEV-2026-001') {
  return render(
    <MemoryRouter initialEntries={[`/devis/${quoteNumber}`]}>
      <Routes>
        <Route path="/devis/:quoteNumber" element={<DevisEditorPage />} />
        <Route path="/" element={<div>Inbox</div>} />
      </Routes>
    </MemoryRouter>
  )
}

describe('DevisEditorPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  it('affiche les informations client et les articles du devis', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => devisFixture,
    } as Response)

    renderEditor()

    await waitFor(() => {
      expect(screen.getByText('Martin Travaux')).toBeInTheDocument()
      expect(screen.getByText('martin@btp.fr')).toBeInTheDocument()
      expect(screen.getByText('Ciment CEM II')).toBeInTheDocument()
    })
  })

  it('valider le devis appelle PUT /api/devis/{quoteNumber}/valider avec statut PRET', async () => {
    vi.mocked(fetch)
      .mockResolvedValueOnce({ ok: true, json: async () => devisFixture } as Response)
      .mockResolvedValueOnce({ ok: true, json: async () => ({ ...devisFixture, status: 'Prêt' }) } as Response)

    const user = userEvent.setup()
    renderEditor()

    await waitFor(() => screen.getByText('Valider le devis'))
    await user.click(screen.getByText('Valider le devis'))

    await waitFor(() => {
      expect(fetch).toHaveBeenCalledTimes(2)
      const [url, options] = vi.mocked(fetch).mock.calls[1] as [string, RequestInit]
      expect(url).toBe('/api/devis/DEV-2026-001/valider')
      expect(options.method).toBe('PUT')
      expect(JSON.parse(options.body as string).statut).toBe('PRET')
    })
  })

  it('le bouton PDF ouvre /api/devis/{quoteNumber}/pdf dans un nouvel onglet', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({ ok: true, json: async () => devisFixture } as Response)
    const openSpy = vi.spyOn(window, 'open').mockImplementation(() => null)

    const user = userEvent.setup()
    renderEditor()

    await waitFor(() => screen.getByText('Télécharger le PDF'))
    await user.click(screen.getByText('Télécharger le PDF'))

    expect(openSpy).toHaveBeenCalledWith('/api/devis/DEV-2026-001/pdf', '_blank')
    openSpy.mockRestore()
  })

  it('affiche une alerte "Devis expiré" si validUntil est dans le passé', async () => {
    const expired = { ...devisFixture, validUntil: '2020-01-01' }
    vi.mocked(fetch).mockResolvedValueOnce({ ok: true, json: async () => expired } as Response)

    renderEditor()

    await waitFor(() => {
      expect(screen.getByText('Devis expiré')).toBeInTheDocument()
    })
  })

  it('le bouton "Valider" est désactivé si statut est déjà "Prêt"', async () => {
    const pret = { ...devisFixture, status: 'Prêt' }
    vi.mocked(fetch).mockResolvedValueOnce({ ok: true, json: async () => pret } as Response)

    renderEditor()

    // Les icônes antd contribuent au nom accessible, on utilise une regex
    await waitFor(() => {
      const btn = screen.getByText('Déjà validé').closest('button')
      expect(btn).toBeDisabled()
    })
  })

  it('rejeter le devis appelle PUT avec statut REJETE', async () => {
    vi.mocked(fetch)
      .mockResolvedValueOnce({ ok: true, json: async () => devisFixture } as Response)
      .mockResolvedValueOnce({ ok: true, json: async () => ({ ...devisFixture, status: 'Rejeté' }) } as Response)

    const user = userEvent.setup()
    renderEditor()

    await waitFor(() => screen.getByText('Rejeter'))
    await user.click(screen.getByText('Rejeter'))

    await waitFor(() => screen.getByText('Confirmer le rejet'))
    await user.click(screen.getByText('Confirmer le rejet'))

    await waitFor(() => {
      const [url, options] = vi.mocked(fetch).mock.calls[1] as [string, RequestInit]
      expect(url).toBe('/api/devis/DEV-2026-001/valider')
      expect(JSON.parse(options.body as string).statut).toBe('REJETE')
    })
  })
})
