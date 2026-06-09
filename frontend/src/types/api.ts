export interface QuoteItem {
  lineNumber: number
  designation: string
  quantity: number
  unitPriceHT: number | null
  discountPercent: number | null
  totalPriceHT: number | null
  category: string
  status: string
}

export interface Devis {
  quoteNumber: string
  createdAt: string
  validUntil: string
  status: string
  priority: string
  subject: string
  clientNom: string | null
  clientEmail: string | null
  emailOriginal: string | null
  totalHT: number | null
  totalTTC: number | null
  tvaRate: number
  clientBudget: number | null
  requestedDeliveryDate: string | null
  confidence: number
  items: QuoteItem[]
  requiredActions: string[]
  recommendations: string[]
  warnings: string[]
  inconsistencies: string[]
}

export interface Stats {
  totalDevis: number
  montantTotalHT: number
  montantMoyenHT: number
  devisParStatut: Record<string, number>
  totalClients: number
  totalProduits: number
  tauxConfidenceMoyen: number
  tauxValidation: number
  tempsMoyenValidationMinutes: number
  totalCorrections: number
  tauxPrecisionIa: number
}

export interface Client {
  clientId: string
  raisonSociale: string
  emailOrigine: string | null
  segment: string | null
  status: string | null
  nombreCommandes: number
  historiqueDevis: string[]
}
