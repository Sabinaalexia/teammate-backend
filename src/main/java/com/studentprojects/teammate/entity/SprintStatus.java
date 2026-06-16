package com.studentprojects.teammate.entity;

public enum SprintStatus {
    PLANNED,      // planificat, nu a început încă
    ACTIVE,       // în desfășurare
    PENDING_CONFIRMATION, // toți membrii au bifat, așteaptă confirmare Scrum Master
    COMPLETED     // confirmat și finalizat, readonly
}