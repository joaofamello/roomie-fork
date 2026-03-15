package br.edu.ufape.roomie.enums;

public enum StudySchedule {
  MORNING("manhã"),
  AFTERNOON("tarde"),
  EVENING("noite"),
  DAWN("madrugada");

  private final String label;

  StudySchedule(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }
}
