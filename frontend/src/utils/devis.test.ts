import { describe, it, expect } from 'vitest'
import {
  normalizeConfidence,
  confidenceColor,
  computeItemTotal,
  computeTotals,
} from './devis'

describe('normalizeConfidence', () => {
  it('convertit une valeur décimale (0-1) en pourcentage entier', () => {
    expect(normalizeConfidence(0.85)).toBe(85)
    expect(normalizeConfidence(0.5)).toBe(50)
    expect(normalizeConfidence(0)).toBe(0)
  })

  it('laisse inchangée une valeur déjà en pourcentage (> 1)', () => {
    expect(normalizeConfidence(85)).toBe(85)
    expect(normalizeConfidence(100)).toBe(100)
    expect(normalizeConfidence(50)).toBe(50)
  })

  it('traite 1.0 comme 100% (valeur limite décimale)', () => {
    expect(normalizeConfidence(1)).toBe(100)
  })

  it('arrondit correctement', () => {
    expect(normalizeConfidence(0.856)).toBe(86)
    expect(normalizeConfidence(0.854)).toBe(85)
  })
})

describe('confidenceColor', () => {
  it('retourne green pour >= 75', () => {
    expect(confidenceColor(75)).toBe('green')
    expect(confidenceColor(100)).toBe('green')
    expect(confidenceColor(76)).toBe('green')
  })

  it('retourne orange pour >= 50 et < 75', () => {
    expect(confidenceColor(50)).toBe('orange')
    expect(confidenceColor(74)).toBe('orange')
  })

  it('retourne red pour < 50', () => {
    expect(confidenceColor(0)).toBe('red')
    expect(confidenceColor(49)).toBe('red')
  })
})

describe('computeItemTotal', () => {
  it('calcule quantité × prix sans remise', () => {
    expect(computeItemTotal({ quantity: 10, unitPriceHT: 100, discountPercent: 0 })).toBe(1000)
  })

  it('applique la remise ligne', () => {
    expect(computeItemTotal({ quantity: 10, unitPriceHT: 100, discountPercent: 10 })).toBe(900)
    expect(computeItemTotal({ quantity: 1, unitPriceHT: 200, discountPercent: 50 })).toBe(100)
  })

  it('gère les valeurs null (prix ou remise non renseignés)', () => {
    expect(computeItemTotal({ quantity: 5, unitPriceHT: null, discountPercent: null })).toBe(0)
    expect(computeItemTotal({ quantity: 5, unitPriceHT: 100, discountPercent: null })).toBe(500)
  })
})

describe('computeTotals', () => {
  const items = [
    { quantity: 2, unitPriceHT: 500, discountPercent: 0 },   // 1 000 €
    { quantity: 1, unitPriceHT: 200, discountPercent: 50 },  //   100 €
  ]

  it('calcule le total HT correct', () => {
    const { totalHT } = computeTotals(items, 0, 20)
    expect(totalHT).toBe(1100)
  })

  it('applique la remise globale sur le HT', () => {
    const { apresRemise } = computeTotals(items, 10, 20)
    expect(apresRemise).toBeCloseTo(990)
  })

  it('calcule TVA et TTC à partir du montant après remise globale', () => {
    const { totalTVA, totalTTC } = computeTotals(items, 0, 20)
    expect(totalTVA).toBeCloseTo(220)
    expect(totalTTC).toBeCloseTo(1320)
  })

  it('retourne zéro partout pour une liste vide', () => {
    const { totalHT, apresRemise, totalTVA, totalTTC } = computeTotals([], 0, 20)
    expect(totalHT).toBe(0)
    expect(apresRemise).toBe(0)
    expect(totalTVA).toBe(0)
    expect(totalTTC).toBe(0)
  })

  it('fonctionne avec un taux de TVA différent (ex: 10%)', () => {
    const { totalTVA, totalTTC } = computeTotals(items, 0, 10)
    expect(totalTVA).toBeCloseTo(110)
    expect(totalTTC).toBeCloseTo(1210)
  })
})
