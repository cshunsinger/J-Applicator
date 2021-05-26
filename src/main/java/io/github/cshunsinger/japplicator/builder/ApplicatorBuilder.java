package io.github.cshunsinger.japplicator.builder;

import io.github.cshunsinger.asmsauce.AsmClassBuilder;
import io.github.cshunsinger.asmsauce.code.CodeInsnBuilderLike;
import io.github.cshunsinger.japplicator.HeadOn;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.cshunsinger.asmsauce.DefinitionBuilders.*;
import static io.github.cshunsinger.asmsauce.MethodNode.method;
import static io.github.cshunsinger.asmsauce.code.CodeBuilders.*;
import static io.github.cshunsinger.asmsauce.modifiers.AccessModifiers.publicOnly;

public class ApplicatorBuilder<Src, Dest> {
    private static final String APPLICATOR_METHOD_NAME = HeadOn.class.getMethods()[0].getName();

    @SuppressWarnings("rawtypes")
    private final AsmClassBuilder<HeadOn> builder;

    public ApplicatorBuilder(Class<Src> sourceClass, Class<Dest> destinationClass) {
        List<SourceNode> sources = SourceNode.createSources(sourceClass);

        final String source = "source";
        final String destination = "destination";

        List<CodeInsnBuilderLike> sourceBuildersList = sources.stream()
            .map(node -> node.buildSource(destinationClass, source, destination))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        builder = new AsmClassBuilder<>(HeadOn.class)
            .withMethod(method(publicOnly(), name(APPLICATOR_METHOD_NAME), parameters(p(source, Object.class), p(destination, Object.class)), type(Object.class),
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
                setVar(source, cast(sourceClass, getVar(source))),
                setVar(destination, cast(destinationClass, getVar(destination))),

                //if(destination == null)
                if_(getVar(destination).isNull()).then(
                    AsmUtils.canBeConstructed(destinationClass) ?
                        //destination = new Object();
                        setVar(destination, instantiate(destinationClass, noParameters())) :
                        //return null;
                        returnValue(stackNull())
                ),

                //if(source == null)
                if_(getVar(source).isNull()).then(
                    //return destination;
                    returnValue(getVar(destination))
                ),

                //... next steps provided by source nodes ...
                block(sourceBuildersList.toArray(CodeInsnBuilderLike[]::new)),

                //return destination;
                returnValue(getVar(destination))
            ));
    }

    @SuppressWarnings("unchecked")
    public HeadOn<Src, Dest> build() {
        return (HeadOn<Src, Dest>)builder.buildInstance();
    }
}