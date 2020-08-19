package com.molva.server.data.validators;

import com.molva.server.data.exceptions.project.ProjectExceptions;
import com.molva.server.data.service.helpers.ProjectValidators;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProjectValidatorsTest {

  @Test
  public void validateProjectNameMustFailIfNameHasInvalidLength() {
    assertThrows(ProjectExceptions.ProjectInvalidNameException.class,
        () -> ProjectValidators.validateProjectName("k"));
    assertThrows(ProjectExceptions.ProjectInvalidNameException.class,
        () -> ProjectValidators.validateProjectName("kit".repeat(66)));
  }

  @Test
  public void validateProjectDescriptionMustFailIfDescriptionHasInvalidLength() {
    assertThrows(ProjectExceptions.ProjectInvalidDescriptionException.class,
        () -> ProjectValidators.validateProjectDescription("k ".repeat(2001)));
    assertThrows(ProjectExceptions.ProjectInvalidDescriptionException.class,
        () -> ProjectValidators.validateProjectDescription("k".repeat(150001)));
  }
}
