import type { QuoteItem } from '../types/api'

/** Normalise le score de confiance : l'API peut retourner 0-1 ou 0-100. */
export function normalizeConfidence(v: number): number {
  return v <= 1 ? Math.round(v * 100) : Math.round(v)
}

/** Retourne la couleur associée à un score de confiance en pourcentage. */
export function confidenceColor(pct: number): string {
  return pct >= 75 ? 'green' : pct >= 50 ? 'orange' : 'red'
}

/** Calcule le total HT d'une ligne (quantité × prix, après remise ligne). */
export function computeItemTotal(
  item: Pick<QuoteItem, 'unitPriceHT' | 'quantity' | 'discountPercent'>
): number {
  const base = (item.unitPriceHT ?? 0) * item.quantity
  return base * (1 - (item.discountPercent ?? 0) / 100)
}

/** Calcule les totaux globaux du devis. */
export function computeTotals(
  items: Pick<QuoteItem, 'unitPriceHT' | 'quantity' | 'discountPercent'>[],
  remiseGlobale: number,
  tvaRate: number
) {
  const totalHT = items.reduce((sum, item) => sum + computeItemTotal(item), 0)
  const apresRemise = totalHT * (1 - remiseGlobale / 100)
  const totalTVA = apresRemise * (tvaRate / 100)
  const totalTTC = apresRemise + totalTVA
  return { totalHT, apresRemise, totalTVA, totalTTC }
}
