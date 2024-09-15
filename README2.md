## Futures: They Found Me

No AWS resources are required for this activity

### Introduction
In our ExecutorServices classroom activity, we wrote code to concurrently
check and update Ring device firmware versions. Today we will expand on that.

In order to comply with new federal privacy laws, Amazon must ensure that all
Ring devices purge their logs after 24 hours. We’ve developed a new Ring
firmware version to satisfy these new requirements. Unfortunately, Ring devices
will be offline during the update. During that time, doorbells won’t ring,
security cameras won’t record, and light switches won’t work.

We have a deadline to comply with the law. Even though we’ve updated our firmware
multiple times and notified customers about the issues, some customers just
haven’t updated yet. We need to find all the customers whose devices are
non-compliant and remotely update their devices to the latest version.

A developer wrote some code to find the devices that need to be updated
for any particular customer and remotely start the device updates.
The QA team says that it runs too slowly. You've been tasked with
improving the runtime.

### Phase 0: Check out the code and make sure it builds

#### Code intro

We'll be reusing a lot of classes from the previous lesson. 
The only new code is `ComplianceEnforcer` in the `com.amazon.ata.futures` package.
Take the `com.amazon.ata.futures` package (make sure to grab both the `src` and `tst` 
packages) and move it into yesterday's code.

Once you've moved your code to yesterday's project, the code should build, but tests 
should fail.

#### Discuss

Look through the code and answer these questions:

1. Which operations in the `ComplianceEnforcer` could be done concurrently?
2. Which of those operations would get the greatest benefit from concurrency?
3. How many tasks do we need to develop to make those operations concurrent?
4. What interface should the tasks implement?

**GOAL:** Become familiar with the code and makes sure that it builds

Phase 0 is complete when:
- You have found the `ComplianceEnforcer` and you have answers to
  the discussion questions above.
- The code is building in your workspace (but tests are failing)

### Phase 1: Big Benefits

As a class, we've decided on the operation that would get the most benefit
from concurrency. Now go and make it so!

We'll be "mob programming" for this phase. One person will be writing the Driver, 
while the other team members will be the Navigators. The principle
here is that "every idea must go through someone else's hands".

**Navigators**, your job is to discuss the idea being coded and guide the
Driver in creating the code. Sometimes that will mean speaking slowly, or
even spelling out the letters needed. Sometimes it'll be an abstract concept.
Always remember the tenets of kindness and collaboration.

**Driver**, your job is to trust the Navigators. Focus on the coding. Ask
for clarification. Tell them when you need clarification and when you need
a moment of silence.

#### Notes for your code
- Your `ExecutorService` should be method-local (not a class member variable in `ComplianceEnforcer`)
- Decide whether to implement the concurrent task as a separate class
  or as a lambda. Either way will work and pass the tests. Use
  whichever style your team prefers.
- You may need to change the parameters or return types of some
  `private` methods
- Remember the guideline from the reading: start concurrent tasks as
  soon as possible, and read their values as late as possible.

**GOAL:** Achieve at least 50% improvement in average runtime 
          (`Phase1Test` tests for this).

#### Phase 1 is complete when:
- You've updated the operation determined by the class to use concurrency.
- `Phase1Test` tests are passing

### Phase 2: Smaller Benefits

As a class, we've decided on the next operation that would most benefit
from concurrency. Continue mob programming to implement the operation
concurrently.

#### Notes for your code
- Your `ExecutorService` should be method-local (not a class member variable in `ComplianceEnforcer`)
- If you implemented the first phase as a separate class, you could
  try using a lambda for this phase (or vice-versa).
- You may need to change the parameters or return types of some
  `private` methods

**GOAL:** Achieve at least a 10% improvement in average runtime
          (`Phase2Test` tests for this).

#### Phase 2 is complete when:
- You have implemented the second operation to be concurrent.
- `Phase2Test` tests are passing

### Extensions

1. Is there a difference between submitting a lambda and submitting a separate class
   to an `ExecutorService`? Write some unit tests and prove it.

2. It may look like **any** loop could be made concurrent. What could be the benefits of
   doing so? Are there any drawbacks?
