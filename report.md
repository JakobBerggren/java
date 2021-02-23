# Report for assignment 3 Group 19

This is the report markdown file of assignment 3 by Group 19. The members for this group assignment are:
E-Joon Ko, Agnes Forsberg, Niklas Tomsic, and Jakob Berggren.

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

The README in jsoniter's java repository is very short, only containing a few repo badges, a link to
external documentation, and a link to the scala implementation of the same project. The
documentation, found at <http://jsoniter.com/java-features.html>, is very comprehensive with respect
to the actual API, but there is nothing about how to build the project or how to run its tests. 

However, the project follows the standard directory layout of Maven and contains a pom.xml document, which also points towards it being a Maven project.

Using IntelliJ IDEA 2018.3.3 (Ultimate Edition), the project was imported via File -> New -> Project from Version Control -> Git.

After cloning, there was a notification in the event log saying that it had detected a Maven project and a prompt for adding it as a Maven Project. Doing so, and setting the Java JDK version to 15.0.2, cleared all remaining errors and question marks (ignoring the alternative Android framework also present in the repository).

From here, it was easy to run all tests via an IntelliJ Run-configuration of "all java". The result was 1192 passed and 566 failed out of a total 1758 tests.

Many of the failures referenced the dependency JavaAssist. Upgrading this package to the [latest version](https://mvnrepository.com/artifact/org.javassist/javassist/3.27.0-GA) caused more tests to succeed, resulting in 1673 passing and 85 failing tests. Many of the remaining failures reference a failing classloader, which is an advanced feature in Java, and as such we feel that this is enough for the purpose of this lab.

The onboarding experience up to this point was all but well-documented, but felt intuitive enough for someone comfortable with code and tech. It made sense to import it with an IDE as a first step, and from there the prompts were easy to follow. Dependencies were all installed automatically by the Maven build tool, and the build succeeded with no errors.


## Complexity

1. What are your results for eight complex functions?


   | NLOC | CCN | token | PARAM | length | CCN by hand | Member    | location                                                        |
   |------|-----|-------|-------|--------|-------------|-----------|-----------------------------------------------------------------|
   | 34   | 16  | 403   | 0     | 54     | 16, 16      | E-Joon Ko, Niklas | decodeFast() [203-256] in Base64.java                   |
   | 60   | 18  | 391   | 1     | 62     | 18, 18      | Caroline, Niklas | chooseImpl() [124-185] in Codegen.java                   |
   | 68   | 16  | 467   | 1     | 68     |             |           | decode_() [271-338] in ReflectionObjectDecoder.java             |
   | 55   | 16  | 406   | 2     | 55     |             |           | getDecodingClassDescriptor() [27-81] in ClassDescriptor.java    |
   | 45   | 16  | 362   | 1     | 45     | 16, 16      | E-Joon Ko, Jakob | updateClassDescriptor() [448-492] in GsonCompatibilityMode.java |
   | 73   | 23  | 527   | 2     | 77     | 23, 23      | Caroline, Jakob  | genReadOp() [195-271] in CodegenImplNative.java                 |
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
   * Exceptions were not taken into account by the lizard tool. Stripping a function of all return-statements and 
     throws did not affect the CCN, leading to the conclusion that lizards assumes every function to have only one exit point.
5. Is the documentation clear w.r.t. all the possible outcomes?
   * The project is not well-documented internally. Comments are few and far-between, and there are no docstrings.

## Refactoring

Plan for refactoring complex code:

Estimated impact of refactoring (lower CC, but other drawbacks?).

Carried out refactoring (optional, P+):

git diff ...

### Codegen::chooseImpl, CCN = 18

Codegen::chooseImpl is used to pick the "proper implementation" to instantiate the object if the type bound to is an interface. The function consists of four consecutive if-statements of varying internal complexity. One is very simple, one is simple, and two are complex. The complex ones have, according to lizard, CCNs of 6 and 7, making them the heavy-hitters of this function. Luckily, they both follow the pattern: "if X, then do Y, eventually either throw an exception or return a value". With few function-local values used, the bodies of these if-statements can be easily lifted into separate functions. This reduces the CCN of the chooseImpl-function to 7 and opens up the possibility for easier testing of these two cases. The clearest draw-back of refactoring this function is that it is old and stable code. When, or if, need should arise, then refactoring should be done then and there instead of here and now. Time could be better spent elsewhere instead of refactoring old code unneccessarily. 

<details><summary>Before</summary>
    
```Java
private static Type chooseImpl(Type type) {
    Type[] typeArgs = new Type[0];
    Class clazz;
    if (type instanceof ParameterizedType) {
        ParameterizedType pType = (ParameterizedType) type;
        clazz = (Class) pType.getRawType();
        typeArgs = pType.getActualTypeArguments();
    } else if (type instanceof WildcardType) {
        return Object.class;
    } else {
        clazz = (Class) type;
    }
    Class implClazz = JsoniterSpi.getTypeImplementation(clazz);
    if (Collection.class.isAssignableFrom(clazz)) {
        Type compType = Object.class;
        if (typeArgs.length == 0) {
            // default to List<Object>
        } else if (typeArgs.length == 1) {
            compType = typeArgs[0];
        } else {
            throw new IllegalArgumentException(
                    "can not bind to generic collection without argument types, " +
                            "try syntax like TypeLiteral<List<Integer>>{}");
        }
        if (clazz == List.class) {
            clazz = implClazz == null ? ArrayList.class : implClazz;
        } else if (clazz == Set.class) {
            clazz = implClazz == null ? HashSet.class : implClazz;
        }
        return GenericsHelper.createParameterizedType(new Type[]{compType}, null, clazz);
    }
    if (Map.class.isAssignableFrom(clazz)) {
        Type keyType = String.class;
        Type valueType = Object.class;
        if (typeArgs.length == 0) {
            // default to Map<String, Object>
        } else if (typeArgs.length == 2) {
            keyType = typeArgs[0];
            valueType = typeArgs[1];
        } else {
            throw new IllegalArgumentException(
                    "can not bind to generic collection without argument types, " +
                            "try syntax like TypeLiteral<Map<String, String>>{}");
        }
        if (clazz == Map.class) {
            clazz = implClazz == null ? HashMap.class : implClazz;
        }
        if (keyType == Object.class) {
            keyType = String.class;
        }
        MapKeyDecoders.registerOrGetExisting(keyType);
        return GenericsHelper.createParameterizedType(new Type[]{keyType, valueType}, null, clazz);
    }
    if (implClazz != null) {
        if (typeArgs.length == 0) {
            return implClazz;
        } else {
            return GenericsHelper.createParameterizedType(typeArgs, null, implClazz);
        }
    }
    return type;
}
```
</details>
<details><summary>After</summary>

```Java
private static Type handleCollection(Type[] typeArgs, Class clazz) {
    Type compType = Object.class;
    if (typeArgs.length == 0) {
        // default to List<Object>
    } else if (typeArgs.length == 1) {
        compType = typeArgs[0];
    } else {
        throw new IllegalArgumentException(
                "can not bind to generic collection without argument types, " +
                        "try syntax like TypeLiteral<List<Integer>>{}");
    }
    Class implClazz = JsoniterSpi.getTypeImplementation(clazz);
    if (clazz == List.class) {
        clazz = implClazz == null ? ArrayList.class : implClazz;
    } else if (clazz == Set.class) {
        clazz = implClazz == null ? HashSet.class : implClazz;
    }
    return GenericsHelper.createParameterizedType(new Type[]{compType}, null, clazz);
}

private static Type handleMap(Type[] typeArgs, Class clazz) {
    Type keyType = String.class;
    Type valueType = Object.class;
    if (typeArgs.length == 0) {
        // default to Map<String, Object>
    } else if (typeArgs.length == 2) {
        keyType = typeArgs[0];
        valueType = typeArgs[1];
    } else {
        throw new IllegalArgumentException(
                "can not bind to generic collection without argument types, " +
                        "try syntax like TypeLiteral<Map<String, String>>{}");
    }
    if (clazz == Map.class) {
        Class implClazz = JsoniterSpi.getTypeImplementation(clazz);
        clazz = implClazz == null ? HashMap.class : implClazz;
    }
    if (keyType == Object.class) {
        keyType = String.class;
    }
    MapKeyDecoders.registerOrGetExisting(keyType);
    return GenericsHelper.createParameterizedType(new Type[]{keyType, valueType}, null, clazz);
}

private static Type chooseImpl(Type type) {
    Type[] typeArgs = new Type[0];
    Class clazz;
    if (type instanceof ParameterizedType) {
        ParameterizedType pType = (ParameterizedType) type;
        clazz = (Class) pType.getRawType();
        typeArgs = pType.getActualTypeArguments();
    } else if (type instanceof WildcardType) {
        return Object.class;
    } else {
        clazz = (Class) type;
    }
    if (Collection.class.isAssignableFrom(clazz)) {
        return handleCollection(typeArgs, clazz);
    }
    if (Map.class.isAssignableFrom(clazz)) {
        return handleMap(typeArgs, clazz);
    }
    Class implClazz = JsoniterSpi.getTypeImplementation(clazz);
    if (implClazz != null) {
        if (typeArgs.length == 0) {
            return implClazz;
        } else {
            return GenericsHelper.createParameterizedType(typeArgs, null, implClazz);
        }
    }
    return type;
}
```
</details>

## Coverage

### Tools

Document your experience in using a "new"/different coverage tool.

How well was the tool documented? Was it possible/easy/difficult to
integrate it with your build environment?

### Your own coverage tool

Show a patch (or link to a branch) that shows the instrumented code to
gather coverage measurements.

Coverage tool branch link: https://github.com/JakobBerggren/java/tree/ManualCodeCoverage

Coverage tool results:

| Coverage % | location                                                        |
|------------|-----------------------------------------------------------------|
| 0.125      | decodeFast() [203-256] in Base64.java                           |
| 0.722      | chooseImpl() [124-185] in Codegen.java                          |
| 0.889      | updateClassDescriptor() [448-492] in GsonCompatibilityMode.java |
| 0.391      | genReadOp() [195-271] in CodegenImplNative.java                 |

### Evaluation

1. What is the quality of your own coverage measurement? Does it take into account ternary operators(condition ? yes : no)
and exceptions, if available in your language?
    *   The quality of our coverage is decent but for sure limited. Since we have taken an approach of updating an array
    data structure each time a specific branch has been reached, it cannot take into account ternary operations or 
    multiple if cases in one line effectively.
    

2. What are the limitations of your tool? How would the instrumentation change if you modify the program?
    *   Our first limitation is as mentioned above not being able to accurately provide coverage to ternary operations or
    multiple if cases in one line. Another limitation with our tool is that our coverage is for the most part hardcoded,
    so using our manual coverage tool on another function other than the four functions in the table will require more
    manual labor from the group. This work would also apply to any updates made in the function, as the "flag" locations
    would also have to be manually updated.    


3. If you have an automated tool, are your results consistent with the ones produced by existing tool(s)?
    *   We can't say for sure. However the results from our manual coverage was quiet different from another existing
        code coverage tool called Codecov, in which updateClassDescriptor() was given a lot coverage score by Codecov,
        while our manual tool determined that the function had a high coverage % of 0.889.

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
