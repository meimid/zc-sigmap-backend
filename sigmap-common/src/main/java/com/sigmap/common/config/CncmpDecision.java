package com.sigmap.common.config;

public enum CncmpDecision {
    APPROVE, // → (AC) Publication
    REJECT, // → Rejet (to CPMP ) for old deployed version (report)
    REJECT_END,// → Rejet (to CPMP or End)
    CI, // → (CNCMP) Révision sous-commission spécialisée
    REPORT, // → Renvoi pour réexamen (to CPMP)
    COND_APPROVE // → Approbation conditionnelle (to CPMP)
}
