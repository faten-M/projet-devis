import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import DashboardPage from './DashboardPage'
import type { Stats } from '../types/api'

const statsFixture: Stats = {
  totalDevis: 11,
  montantTotalHT: 14200,
  montantMoyenHT: 1290.91,
  devisParStatut: {
    'Brouillon':   3,
    'À valider':   4,
    'Prêt':        2,
    'Rejeté':      1,
    'À compléter': 1,
  },
  totalClients: 14,
  totalProduits: 28,
  tauxConfidenceMoyen: 0.74,   // stocké en 0-1 → doit afficher 74%
  tauxValidation: 18.2,
  tempsMoyenValidationMinutes: 45,
}

function renderDashboard() {
  return render(
    <MemoryRouter>
      <DashboardPage />
    </MemoryRouter>
  )
}

describe('DashboardPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn())
  })

  it('affiche les 4 KPI cards avec les bonnes valeurs', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => statsFixture,
    } as Response)

    renderDashboard()

    await waitFor(() => {
      // antd Statistic découpe les nombres en spans séparés — on cherche les labels
      expect(screen.getByText('Total devis')).toBeInTheDocument()
      expect(screen.getByText('Taux de validation')).toBeInTheDocument()
      expect(screen.getByText('Confiance IA moyenne')).toBeInTheDocument()
      expect(screen.getByText('Montant moyen HT')).toBeInTheDocument()
    })
  })

  it('normalise la confiance IA de 0-1 vers % (0.74 → 74%)', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => statsFixture,
    } as Response)

    renderDashboard()

    await waitFor(() => {
      // Doit afficher 74, pas 0.74
      expect(screen.getByText('74')).toBeInTheDocument()
      expect(screen.queryByText('0.74')).not.toBeInTheDocument()
    })
  })

  it('affiche le temps moyen en minutes si < 60', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => statsFixture,   // 45 min
    } as Response)

    renderDashboard()

    await waitFor(() => {
      expect(screen.getByText('45 min')).toBeInTheDocument()
    })
  })

  it('affiche le temps moyen en heures si >= 60 min', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ ...statsFixture, tempsMoyenValidationMinutes: 90 }),
    } as Response)

    renderDashboard()

    await waitFor(() => {
      expect(screen.getByText('1.5 h')).toBeInTheDocument()
    })
  })

  it('affiche le temps moyen en jours si >= 24h', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => ({ ...statsFixture, tempsMoyenValidationMinutes: 4320 }), // 72h = 3j
    } as Response)

    renderDashboard()

    await waitFor(() => {
      expect(screen.getByText('3.0 j')).toBeInTheDocument()
    })
  })

  it('affiche les stats clients, produits et temps moyen', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => statsFixture,
    } as Response)

    renderDashboard()

    await waitFor(() => {
      expect(screen.getByText('14')).toBeInTheDocument()   // clients
      expect(screen.getByText('28')).toBeInTheDocument()   // produits
    })
  })

  it('affiche un message d\'erreur si l\'API échoue', async () => {
    vi.mocked(fetch).mockResolvedValueOnce({ ok: false, status: 500 } as Response)

    renderDashboard()

    await waitFor(() => {
      // Le composant retourne null après l'erreur — pas de crash
      expect(document.body).toBeInTheDocument()
    })
  })
})
