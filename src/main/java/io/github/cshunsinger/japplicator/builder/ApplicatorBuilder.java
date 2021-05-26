package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.japplicator.HeadOn;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.cshunsinger.asmsauce.ConstructorNode.constructor;
import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;

public class ApplicatorBuilder<Src, Dest> {
    private static final String APPLICATOR_METHOD_NAME = HeadOn.class.getMethods()[0].getName();
    private static final int P_SRC = 1;
    private static final int P_DEST = 2;

    private final AsmClassBuilder<HeadOn> builder;

    public ApplicatorBuilder(Class<Src> sourceClass, Class<Dest> destinationClass) {
        List<SourceNode> sources = SourceNode.createSources(sourceClass);

        List<CodeInsnBuilderLike> sourceBuildersList = sources.stream()
            .map(node -> node.buildSource(destinationClass, P_SRC, P_DEST))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        builder = new AsmClassBuilder<>(HeadOn.class)
            .withConstructor(constructor(publicOnly(), noParameters(), //Empty constructor
                //super();
                superConstructor(HeadOn.class, noParameters()),
                //return;
                returnVoid()
            ))
            .withMethod(method(publicOnly(), name(APPLICATOR_METHOD_NAME), parameters(Object.class, Object.class), type(Object.class),
                /*
                 * public Object applyDirectlyToTheForehead(Object source, Object destination) {
                 *     if(destination == null) {
                 *         //This statement is ONLY if the destination object can be constructed
                 *         destination = new Object();
                 *         //This return statement is ONLY if the destination object cannot be constructed
                 *         return null;
                 *     }
                 *
                 *     if(source == null)
                 *         return destination;
                 *
                 *     ... next steps provided by source nodes ...
                 *
                 *    return destination;
                 * }
                 */

                //Just come casting
                setVar(P_SRC, cast(sourceClass, getVar(P_SRC))),
                setVar(P_DEST, cast(destinationClass, getVar(P_DEST))),

                //if(destination == null)
                if_(getVar(P_DEST).isNull()).then(
                    AsmUtils.canBeConstructed(destinationClass) ?
                        //destination = new Object();
                        setVar(P_DEST, instantiate(destinationClass, noParameters())) :
                        //return null;
                        returnValue(stackNull())
                ),

                //if(source == null)
                if_(getVar(P_SRC).isNull()).then(
                    //return destination;
                    returnValue(getVar(P_DEST))
                ),

                //... next steps provided by source nodes ...
                sourceBuildersList.isEmpty() ? null : block(sourceBuildersList.toArray(CodeInsnBuilderLike[]::new)),

                //return destination;
                returnValue(getVar(P_DEST))
            ));
    }

    public HeadOn build() {
        return builder.buildInstance();
    }
}