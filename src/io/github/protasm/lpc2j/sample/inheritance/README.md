# Inheritance validation samples

These LPC objects exercise inheritance, initialization, dispatch, and layout through the full
compiler pipeline.

## Objects
- `standalone_basic.c`: Standalone object with fields and methods. Expected to compile and run.
- `inherit_parent.c`: Parent object with initialized fields and methods. Expected to compile and
  serve as the superclass for the child sample.
- `inherit_child.c`: Inherits from `inherit_parent.c`, overrides `shout`, calls the parent
  implementation explicitly, defines child-only fields, and shadows the parent's `shadowed_field`.
  Expected to compile and run.
- `no_inherit_simple.c`: Independent object with no inheritance. Expected to compile and run.
- `invalid_duplicate_field.c`: Declares the same field twice. Expected to fail semantic validation
  with a duplicate field error.

## Notes
- The regression harness (`PipelineRegressionTests#inheritanceSamplesExercisePipeline`) loads these
  files through the full pipeline, reflects on the resulting classes, and validates dispatch,
  initialization ordering, field layout, and duplicate detection behavior. The current run passes
  without discrepancies.
