# Report for assignment 3

This is a template for your report. You are free to modify it as needed.
It is not required to use markdown for your report either, but the report
has to be delivered in a standard, cross-platform format.

## Project

Name: Jsoniter

URL: https://github.com/json-iterator/java

jsoniter (json-iterator) is fast and flexible JSON parser available in Java and Go. Good deal of the code is ported
from dsljson, and jsonparser.

## Onboarding experience

Did it build and run as documented?
    
See the assignment for details; if everything works out of the box,
there is no need to write much here. If the first project(s) you picked
ended up being unsuitable, you can describe the "onboarding experience"
for each project, along with reason(s) why you changed to a different one.


## Complexity

1. What are your results for eight complex functions?


   | NLOC | CCN | token | PARAM | length | CCN by hand | Member    | location                                                        |
   |------|-----|-------|-------|--------|-------------|-----------|-----------------------------------------------------------------|
   | 34   | 16  | 403   | 0     | 54     | 16          | E-Joon Ko | decodeFast() [203-256] in Base64.java                           |
   | 60   | 18  | 391   | 1     | 62     | 18, 8       | Caroline, Niklas | chooseImpl() [124-185] in Codegen.java = 18                     |
   | 68   | 16  | 467   | 1     | 68     |             |           | decode_() [271-338] in ReflectionObjectDecoder.java             |
   | 55   | 16  | 406   | 2     | 55     |             |           | getDecodingClassDescriptor() [27-81] in ClassDescriptor.java    |
   | 45   | 16  | 362   | 1     | 45     | 16          | E-Joon Ko | updateClassDescriptor() [448-492] in GsonCompatibilityMode.java |
   | 73   | 23  | 527   | 2     | 77     | 23          | Caroline  | genReadOp() [195-271] in CodegenImplNative.java                 |
   | 84   | 17  | 576   | 2     | 88     |             |           | createEncoder() [245-332] in GsonCompatibilityMode.java         |
   | 97   | 17  | 735   | 1     | 104    |             |           | genObjectUsingHash() [10-113] in CodegenImplObjectHash.java     |
2. Are the functions just complex, or also long?
   * Some functions are both long and complex. However, there are definitely cases such as decodeFast() in Base64.java
     which have a Cyclomatic Complexity of 16 with only 34 lines of code. However, with further inspection, it can be seen
     that this is the case due to there being 2 separate cases of a double if statement written in 1 line of code, which
     increases the Cyclomatic Complexity while keeping the number of lines small. However, for most cases, a larger
     Cyclomatic Complexity will in general result in more lines of code.
3. What is the purpose of the functions?
   * The purpose of a lot of these functions are for specific parsing scenarios. Therefore, the high number of
     Cyclomatic Complexity in these codes is understandable as parsing includes a lot of "if" cases due to the high
     amount of edge cases.
4. Are exceptions taken into account in the given measurements?
5. Is the documentation clear w.r.t. all the possible outcomes?

## Refactoring

Plan for refactoring complex code:

Estimated impact of refactoring (lower CC, but other drawbacks?).

Carried out refactoring (optional, P+):

git diff ...

## Coverage

### Tools

Document your experience in using a "new"/different coverage tool.

How well was the tool documented? Was it possible/easy/difficult to
integrate it with your build environment?

### Your own coverage tool

Show a patch (or link to a branch) that shows the instrumented code to
gather coverage measurements.

The patch is probably too long to be copied here, so please add
the git command that is used to obtain the patch instead:

git diff ...

What kinds of constructs does your tool support, and how accurate is
its output?

### Evaluation

1. How detailed is your coverage measurement?

2. What are the limitations of your own tool?

3. Are the results of your tool consistent with existing coverage tools?

## Coverage improvement

Show the comments that describe the requirements for the coverage.

Report of old coverage: [link]

Report of new coverage: [link]

Test cases added:

git diff ...

Number of test cases added: two per team member (P) or at least four (P+).

## Self-assessment: Way of working

Current state according to the Essence standard: ...

Was the self-assessment unanimous? Any doubts about certain items?

How have you improved so far?

Where is potential for improvement?

## Overall experience

What are your main take-aways from this project? What did you learn?

Is there something special you want to mention here?
