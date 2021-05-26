package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.japplicator.annotation.FieldIdentifier;
import io.github.cshunsinger.japplicator.annotation.Nested;
import io.github.cshunsinger.japplicator.util.ReflectionsUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.japplicator.builder.AsmUtils.memberIdentifierName;
import static io.github.cshunsinger.japplicator.util.ReflectionsUtils.findGetterMethodForField;
import static org.apache.commons.lang3.StringUtils.capitalize;

@Slf4j
@RequiredArgsConstructor
public class SourceNode {
    private final String fieldName;
    private final Method getter;
    private final List<SourceNode> nestedNodes;

    private SourceNode(String fieldName, Method getter) {
        this(fieldName, getter, null);
    }

    /**
     * Builds the bytecode to access values from a source object.
     *
     * @param fromVar The index of the local variable that holds the current source object (at whatever the current level of nesting)
     * @param toParam The index of the destination parameter of the method being generated.
     */
    public CodeInsnBuilderLike buildSource(Class<?> destinationClass, String fromVar, String toParam) {
        String nextFromVar = fromVar + capitalize(fieldName);

        if(fieldName == null) {
            //This is a nested step
            /*
             * //Assume that fromVar has already been null-checked
             * var fromVar2 = fromVar.getNestedValue();
             * if(fromVar2 != null) {
             *     ... next code steps ...
             * }
             */

            //nestedNodes is guaranteed to NOT be empty because of the logic in the "createSources" method which will
            //specifically NOT create a SourceNode with a null fieldName and an empty nestedNodes list.

            return block(
                //var fromVar2 = fromVar.getNestedValue();
                setVar(nextFromVar, getVar(fromVar).invoke(getter.getDeclaringClass(), getter)),
                //if(fromVar2 != null)
                if_(getVar(nextFromVar).isNotNull()).then(
                    //... next code steps ...
                    nestedNodes.stream()
                        .map(node -> node.buildSource(destinationClass, nextFromVar, toParam))
                        .toArray(CodeInsnBuilderLike[]::new)
                )
            );
        }
        else {
            //This is not a nested step

            //... destination code steps ...
            List<CodeInsnBuilderLike> destinationCodeBuilders = DestinationNode.createDestinationsForField(fieldName, destinationClass)
                .stream()
                .map(node -> node.buildDestination(getter.getReturnType(), toParam, nextFromVar))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            if(destinationCodeBuilders.isEmpty())
                return null;

            if(getter.getReturnType().isPrimitive()) {
                /*
                 * var fromVar2 = fromVar.getSourceValue();
                 * ... destination code steps ...
                 */
                return block(
                    setVar(nextFromVar, getVar(fromVar).invoke(getter.getDeclaringClass(), getter)),
                    block(
                        destinationCodeBuilders.toArray(CodeInsnBuilderLike[]::new)
                    )
                );
            }
            else {
                /*
                 * var fromVar2 = fromVar.getSourceValue();
                 * if(fromVar2 != null) {
                 *     ... destination code steps ...
                 * }
                 */
                return block(
                    //var fromVar2 = fromVar.getSourceValue();
                    setVar(nextFromVar, getVar(fromVar).invoke(getter.getDeclaringClass(), getter)),
                    //if(fromVar2 != null)
                    if_(getVar(nextFromVar).isNotNull()).then(
                        //... destination code steps ...
                        destinationCodeBuilders.toArray(CodeInsnBuilderLike[]::new)
                    )
                );
            }
        }
    }

    public static List<SourceNode> createSources(Class<?> type) {
        FieldIdentifier defaults = type.getAnnotation(FieldIdentifier.class);
        Stream<SourceNode> fieldNodes = Stream.of(type.getDeclaredFields())
            .map(field -> {
                String identifierName = memberIdentifierName(field, defaults);
                if(identifierName == null)
                    return null;

                Method getterMethod = findGetterMethodForField(type, field);
                if(getterMethod == null) {
                    log.info("Skipping field {} identified as {} because no accessor method was found.", field.getName(), identifierName);
                    return null;
                }

                log.info("Found field {} identified as {}.", field.getName(), identifierName);
                return new SourceNode(identifierName, getterMethod);
            });
        Stream<SourceNode> methodNodes = Stream.of(type.getDeclaredMethods())
            .filter(method -> ReflectionsUtils.getInvalidGetterMethodReason(method) == null)
            .map(method -> {
                String identifierName = memberIdentifierName(method, null);
                if(identifierName == null)
                    return null;

                log.info("Found getter method {} identified as {}.", method.getName(), identifierName);
                return new SourceNode(identifierName, method);
            });

        Stream<SourceNode> nestedFields = Stream.of(type.getDeclaredFields())
            .filter(field -> field.isAnnotationPresent(Nested.class))
            .map(field -> {
                Method getterMethod = findGetterMethodForField(type, field);
                if(getterMethod == null) {
                    log.info("Skipping nested field {} because no accessor method was found.", field.getName());
                    return null;
                }

                log.info("Found @Nested field {}.", field.getName());
                Class<?> nestedType = field.getType();
                List<SourceNode> nestedSources = createSources(nestedType);
                return nestedSources.isEmpty() ? null : new SourceNode(null, getterMethod, nestedSources);
            });
        Stream<SourceNode> nestedMethods = Stream.of(type.getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(Nested.class))
            .filter(method -> ReflectionsUtils.getInvalidGetterMethodReason(method) == null)
            .map(method -> {
                Class<?> nestedType = method.getReturnType();
                List<SourceNode> nestedSources = createSources(nestedType);
                return nestedSources.isEmpty() ? null : new SourceNode(null, method, nestedSources);
            });

        return Stream.of(fieldNodes, methodNodes, nestedFields, nestedMethods)
            .flatMap(nodes -> nodes.filter(Objects::nonNull))
            .collect(Collectors.toList());
    }
}