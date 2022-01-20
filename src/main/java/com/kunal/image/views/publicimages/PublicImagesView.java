package com.kunal.image.views.publicimages;

import com.kunal.image.data.entity.ImageEntity;
import com.kunal.image.data.service.ImageEntityService;
import com.kunal.image.views.MainLayout;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import javax.annotation.security.PermitAll;

@PageTitle("Public Images")
@Route(value = "public-images/:imageImageEntityID?/:action?(edit)", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class PublicImagesView extends Div {


    private Grid<ImageEntity> grid = new Grid<>(ImageEntity.class, false);

    private ImageEntityService imageEntityService;

    public PublicImagesView(@Autowired ImageEntityService imageEntityService) {
        this.imageEntityService = imageEntityService;
        addClassNames("public-images-view", "flex", "flex-col", "h-full");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();
        splitLayout.setSizeFull();

        createGridLayout(splitLayout);

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
        grid.setItems(query -> imageEntityService.listPublicImages(
                        PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setHeightFull();
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setId("grid-wrapper");
        wrapper.setWidthFull();
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

}
