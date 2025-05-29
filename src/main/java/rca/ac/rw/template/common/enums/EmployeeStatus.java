package rca.ac.rw.template.common.enums;

/**
 * Defines the operational status of an Employee account.
 */
public enum EmployeeStatus {
    ACTIVE,   // Employee account is active and can be used
    DISABLED, // Employee account is disabled (e.g., resigned, on long leave without pay)
    PENDING   // Employee account created but awaiting final activation/onboarding
}