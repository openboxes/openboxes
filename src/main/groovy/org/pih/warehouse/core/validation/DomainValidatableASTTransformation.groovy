package org.pih.warehouse.core.validation

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * Modifies all Grails domain entities that implement DomainValidatable to shift the trait declaration to
 * the end (right side) of the list. This is performed at compile time via AST Transformation.
 *
 * For example, if you declare a domain with:
 *
 * class X implements DomainValidatable<XValidator>
 *
 * Without DomainValidatableASTTransformation, after Grails and Groovy AST Transforms are applied it will become:
 *
 * class X implements DomainValidatable<XValidator>,
 *                    GroovyObject,
 *                    grails.artefact.DomainClass,
 *                    grails.web.databinding.WebDataBinding,
 *                    grails.gorm.Entity<X>,
 *                    org.grails.datastore.mapping.dirty.checking.DirtyCheckable,
 *                    org.grails.datastore.gorm.GormEntity<X>
 *
 * This means that even though we override GORM's validate() behaviour in DomainValidatable, it will ignore our
 * trait and still use GormEntity, (which was added to the implements list at compile time). This is because in
 * a multiple inheritance structure, traits/interfaces declared later in the list will take priority.
 *
 * With DomainValidatableASTTransformation, the class declaration instead becomes:
 *
 * class X implements GroovyObject,
 *                    grails.artefact.DomainClass,
 *                    grails.web.databinding.WebDataBinding,
 *                    grails.gorm.Entity<X>,
 *                    org.grails.datastore.mapping.dirty.checking.DirtyCheckable,
 *                    org.grails.datastore.gorm.GormEntity<X>,
 *                    DomainValidatable<XValidator>
 *
 * So the validate() override in DomainValidatable will take priority.
 *
 * All of this is to achieve the ability to append the validation that is performed on Domain.save() with additional,
 * custom constraints. See {@link DomainValidatable} for further details.
 *
 * All Grails transformations are performed during the SEMANTIC_ANALYSIS phase, so it is safe to add custom AST
 * Transforms during the CANONICALIZATION phase, which comes afterwards. (This is what the phase is designed for.)
 *
 * If we eventually move away from Grails to vanilla SpringBoot, then this class should be replaced with
 * a @ControllerAdvice component that wires in all Validator components and has a @InitBinder that picks out
 * the validator associated with the binder.getTarget().getClass() and calls binder.addValidators(validator).
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class DomainValidatableASTTransformation implements ASTTransformation {

    // We don't need to do this for ObjectValidatable classes because Grails doesn't do any AST Transforms for those.
    private static final String TRAIT_NAME = DomainValidatable.name

    void visit(ASTNode[] nodes, SourceUnit source) {
        for (classNode in source.AST.classes) {
            ClassNode traitToReorder = classNode.interfaces.find { it.name == TRAIT_NAME }
            if (!traitToReorder) {
                return
            }

            // We put the trait at the end (right side) of the "implements" list so that it takes priority.
            // This is a groovy rule. Given a multiple inheritance structure like "X implements A, B",
            // if A and B have two identical method declarations, the method from B will be used.
            List<ClassNode> reordered = classNode.interfaces.toList()
            reordered.remove(traitToReorder)
            reordered.add(traitToReorder)
            classNode.setInterfaces(reordered as ClassNode[])
        }
    }
}