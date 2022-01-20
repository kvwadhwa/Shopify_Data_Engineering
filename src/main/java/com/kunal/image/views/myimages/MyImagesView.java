package com.kunal.image.views.myimages;

import com.kunal.image.data.entity.ImageEntity;
import com.kunal.image.data.service.ImageEntityService;
import com.kunal.image.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import elemental.json.Json;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.util.UriUtils;

@PageTitle("My Images")
@Route(value = "my-images/:imageImageEntityID?/:action?(edit)", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class MyImagesView extends Div implements BeforeEnterObserver {

    private final String IMAGEENTITY_ID = "imageEntityID";
    private final String IMAGEENTITY_EDIT_ROUTE_TEMPLATE = "my-images/%d/edit";

    private Grid<ImageEntity> grid = new Grid<>(ImageEntity.class, false);

    @PropertyId("title")
    private TextField title;
    private Upload image;
    private Image imagePreview;
    @PropertyId("isPublic")
    private Checkbox isPublic;
    private TextField username;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");
    private Button delete = new Button("Delete");

    private BeanValidationBinder<ImageEntity> binder;

    private ImageEntity imageEntity;

    private ImageEntityService imageEntityService;

    public MyImagesView(@Autowired ImageEntityService imageEntityService) {
        this.imageEntityService = imageEntityService;
        addClassNames("my-images-view", "flex", "flex-col", "h-full");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("title").setAutoWidth(true);
        TemplateRenderer<ImageEntity> imageRenderer = TemplateRenderer
                .<ImageEntity>of("<img style='height: 64px' src='[[item.image]]' />")
                .withProperty("image", ImageEntity::getImage);
        grid.addColumn(imageRenderer).setHeader("Image").setWidth("68px").setFlexGrow(0);

        TemplateRenderer<ImageEntity> isPublicRenderer = TemplateRenderer.<ImageEntity>of(
                "<vaadin-icon hidden='[[!item.isPublic]]' icon='vaadin:check' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-primary-text-color);'></vaadin-icon><vaadin-icon hidden='[[item.isPublic]]' icon='vaadin:minus' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-disabled-text-color);'></vaadin-icon>")
                .withProperty("isPublic", ImageEntity::isIsPublic);
        grid.addColumn(isPublicRenderer).setHeader("Is Public").setAutoWidth(true);

        grid.addColumn("username").setAutoWidth(true);
        grid.setItems(query -> imageEntityService.listUserImages(PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setHeightFull();

        // Configure Form
        binder = new BeanValidationBinder<>(ImageEntity.class);

        // Bind fields. This where you'd define e.g. validation rules
        binder.bindInstanceFields(this);

        attachImageUpload(image, imagePreview);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.imageEntity == null) {
                    this.imageEntity = new ImageEntity();
                }
                binder.writeBean(this.imageEntity);
                this.imageEntity.setImage(imagePreview.getSrc());

                imageEntityService.update(this.imageEntity);
                clearForm();
                refreshGrid();
                Notification.show("ImageEntity details stored.");
                UI.getCurrent().navigate(MyImagesView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the imageEntity details.");
            }
        });

        delete.addClickListener(e -> {
            grid.getSelectedItems().forEach((ImageEntity entity) -> {
                imageEntityService.delete(entity.getId());
            });
            refreshGrid();
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<Integer> imageEntityId = event.getRouteParameters().getInteger(IMAGEENTITY_ID);
        if (imageEntityId.isPresent()) {
            Optional<ImageEntity> imageEntityFromBackend = imageEntityService.get(imageEntityId.get());
            if (imageEntityFromBackend.isPresent()) {
                populateForm(imageEntityFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested imageEntity was not found, ID = %d", imageEntityId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(MyImagesView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("flex flex-col");
        editorLayoutDiv.setWidth("400px");

        Div editorDiv = new Div();
        editorDiv.setClassName("p-l flex-grow");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        title = new TextField("Title");
        Label imageLabel = new Label("Image");
        imagePreview = new Image();
        imagePreview.setWidth("100%");
        image = new Upload();
        image.getStyle().set("box-sizing", "border-box");
        image.getElement().appendChild(imagePreview.getElement());
        isPublic = new Checkbox("Is Public");
        isPublic.getStyle().set("padding-top", "var(--lumo-space-m)");
        Component[] fields = new Component[]{title, imageLabel, image, isPublic};

        for (Component field : fields) {
            ((HasStyle) field).addClassName("full-width");
        }
        formLayout.add(fields);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("w-full flex-wrap bg-contrast-5 py-s px-l");
        buttonLayout.setSpacing(true);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        buttonLayout.add(save, cancel, delete);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void attachImageUpload(Upload upload, Image preview) {
        ByteArrayOutputStream uploadBuffer = new ByteArrayOutputStream();
        upload.setAcceptedFileTypes("image/*");
        upload.setReceiver((fileName, mimeType) -> {
            return uploadBuffer;
        });
        upload.addSucceededListener(e -> {
            String mimeType = e.getMIMEType();
            String base64ImageData = Base64.getEncoder().encodeToString(uploadBuffer.toByteArray());
            String dataUrl = "data:" + mimeType + ";base64,"
                    + UriUtils.encodeQuery(base64ImageData, StandardCharsets.UTF_8);
            upload.getElement().setPropertyJson("files", Json.createArray());
            preview.setSrc(dataUrl);
            uploadBuffer.reset();
        });
        preview.setVisible(false);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(ImageEntity value) {
        this.imageEntity = value;
        binder.readBean(this.imageEntity);
        this.imagePreview.setVisible(value != null);
        if (value == null) {
            this.imagePreview.setSrc("");
        } else {
            this.imagePreview.setSrc(value.getImage());
        }

    }
}
