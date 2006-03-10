package org.openvpms.web.component.im.edit;

import java.util.List;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.event.WindowPaneEvent;
import nextapp.echo2.app.event.WindowPaneListener;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.ModifiableProperty;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.create.IMObjectCreatorListener;
import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserDialog;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * An editor for {@link EntityRelationship}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class RelationshipEditor extends AbstractIMObjectEditor {

    /**
     * Editor for the source of the relationship.
     */
    private ObjectReferenceEditor _source;

    /**
     * Editor for the target of the relationship.
     */
    private ObjectReferenceEditor _target;


    /**
     * Construct a new <code>RelationshipEditor</code>.
     *
     * @param relationship the relationship
     * @param parent       the parent object
     * @param descriptor   the parent descriptor
     * @param showAll      if <code>true</code> show optional and required
     *                     fields; otherwise show required fields.
     */
    protected RelationshipEditor(EntityRelationship relationship, IMObject parent,
                                 NodeDescriptor descriptor, boolean showAll) {
        super(relationship, parent, descriptor, showAll);
        IMObject source;
        IMObject target;

        ArchetypeDescriptor archetype = getArchetypeDescriptor();
        NodeDescriptor sourceDesc = archetype.getNodeDescriptor("source");
        NodeDescriptor targetDesc = archetype.getNodeDescriptor("target");

        source = Entity.getObject(relationship.getSource(), sourceDesc);
        target = Entity.getObject(relationship.getTarget(), targetDesc);

        IMObject edited = Context.getInstance().getCurrent();
        boolean srcReadOnly = true;
        if (source == null || !source.equals(edited)) {
            srcReadOnly = false;
        }

        _source = getEditor(relationship, sourceDesc, srcReadOnly);
        if (source != null && relationship.getSource() == null) {
            _source.setObject(source);
        }

        boolean targetReadOnly = true;
        if (target == null || !target.equals(edited) || target.equals(source)) {
            targetReadOnly = false;
        }

        _target = getEditor(relationship, targetDesc, targetReadOnly);
        if (target != null && relationship.getTarget() == null) {
            _target.setObject(target);
        }
    }

    /**
     * Create a new editor for an object, if it can be edited by this class.
     *
     * @param object     the object to edit
     * @param parent     the parent object. May be <code>null</code>
     * @param descriptor the parent descriptor. May be <code>null</cocde>
     * @param showAll    if <code>true</code> show optional and required fields;
     *                   otherwise show required fields.
     * @return a new editor for <code>object</code>, or <code>null</code> if it
     *         cannot be edited by this
     */
    public static IMObjectEditor create(IMObject object, IMObject parent,
                                        NodeDescriptor descriptor, boolean showAll) {
        IMObjectEditor result = null;
        if (object instanceof EntityRelationship) {
            result = new RelationshipEditor((EntityRelationship) object, parent,
                                            descriptor, showAll);
        }
        return result;
    }

    /**
     * Creates the layout strategy.
     *
     * @param showAll if <code>true</code> show required and optional fields;
     *                otherwise show required fields.
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy(boolean showAll) {
        return new LayoutStrategy(showAll);
    }

    /**
     * Returns an editor for one side of the relationship.
     *
     * @param relationship the relationship
     * @param descriptor   the descriptor of the node to edit
     * @param readOnly     determines if the node is read-only
     */
    protected ObjectReferenceEditor getEditor(EntityRelationship relationship,
                                              NodeDescriptor descriptor,
                                              boolean readOnly) {

        ModifiableProperty property
                = new ModifiableProperty(relationship, descriptor);

        getModifiableSet().add(relationship, property);
        return new Entity(property, descriptor, readOnly);
    }

    /**
     * Pops up a dialog to select an entity.
     *
     * @param entity the entity wrapper
     */
    protected void onSelect(final Entity entity) {
        NodeDescriptor descriptor = entity.getDescriptor();
        Query query = QueryFactory.create(descriptor.getArchetypeRange());
        final Browser browser = new Browser(query);
        String title = Messages.get("imobject.select.title",
                                    descriptor.getDisplayName());
        final BrowserDialog popup = new BrowserDialog(title, browser, true);

        popup.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                if (popup.createNew()) {
                    onCreate(entity);
                } else {
                    IMObject object = popup.getSelected();
                    if (object != null) {
                        entity.setObject(object);
                    }
                }
            }
        });

        popup.show();
    }

    /**
     * Invoked when the 'new' button is pressed.
     *
     * @param entity describes the type of object to create
     */
    protected void onCreate(final Entity entity) {
        IMObjectCreatorListener listener = new IMObjectCreatorListener() {
            public void created(IMObject object) {
                onCreated(object, entity);
            }
        };

        NodeDescriptor descriptor = entity.getDescriptor();
        IMObjectCreator.create(descriptor.getDisplayName(),
                               descriptor.getArchetypeRange(), listener);
    }


    /**
     * Invoked when an object is created. Pops up an editor to edit it.
     *
     * @param object the object to edit
     * @param entity the entity to associate the object with, on completion of
     *               editing
     */
    private void onCreated(IMObject object, final Entity entity) {
        final IMObjectEditor editor
                = IMObjectEditorFactory.create(object, true);
        final EditDialog dialog = new EditDialog(editor);
        dialog.addWindowPaneListener(new WindowPaneListener() {
            public void windowPaneClosing(WindowPaneEvent event) {
                onEditCompleted(editor, entity);
            }
        });

        dialog.show();
    }

    /**
     * Invoked when the editor is closed.
     *
     * @param editor the editor
     * @param entity the entity to associate the object with
     */
    protected void onEditCompleted(IMObjectEditor editor, Entity entity) {
        if (!editor.isCancelled() && !editor.isDeleted()) {
            entity.setObject(editor.getObject());
        }
    }

    /**
     * EntityRelationship layout strategy. Displays the source and target nodes
     * before any others.
     */
    private class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Construct a new <code>LayoutStrategy</code>.
         *
         * @param showOptional if <code>true</code> show optional fields as well
         *                     as mandatory ones.
         */
        public LayoutStrategy(boolean showOptional) {
            ChainedNodeFilter filter = new ChainedNodeFilter();
            filter.add(new BasicNodeFilter(showOptional));
            filter.add(new NamedNodeFilter("source", "target"));
            setNodeFilter(filter);
        }

        /**
         * Lays out child components in a 2x2 grid.
         *
         * @param object      the parent object
         * @param descriptors the child descriptors
         * @param container   the container to use
         * @param factory     the component factory
         */
        @Override
        protected void doSimpleLayout(IMObject object,
                                      List<NodeDescriptor> descriptors,
                                      Component container,
                                      IMObjectComponentFactory factory) {
            Grid grid = GridFactory.create(4);
            add(grid, _source.getDisplayName(), _source.getComponent());
            add(grid, _target.getDisplayName(), _target.getComponent());
            doGridLayout(object, descriptors, grid, factory);
            container.add(grid);
        }

    }

    /**
     * Editor for a source/target entity in a relationship.
     */
    private class Entity extends ObjectReferenceEditor {

        /**
         * Construct a new <code>Entity</code>.
         *
         * @param property   the reference property
         * @param descriptor the entity descriptor
         * @param readOnly   if <code>true<code> don't render the select button
         */
        public Entity(Property property, NodeDescriptor descriptor,
                      boolean readOnly) {
            super(property, descriptor, readOnly);
        }

        /**
         * Pops up a dialog to select an object.
         */
        @Override
        protected void onSelect() {
            // override default behaviour to enable creation of objects.
            RelationshipEditor.this.onSelect(this);
        }

    }

}
