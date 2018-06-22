package org.wickedsource.budgeteer.web.components.burntable.table;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.EmptyDataProvider;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.joda.money.Money;
import org.wickedsource.budgeteer.service.record.RecordService;
import org.wickedsource.budgeteer.service.record.WorkRecord;
import org.wickedsource.budgeteer.service.record.WorkRecordFilter;
import org.wickedsource.budgeteer.web.ClassAwareWrappingModel;
import org.wickedsource.budgeteer.web.components.burntable.filter.FilteredRecordsModel;
import org.wickedsource.budgeteer.web.components.customFeedback.CustomFeedbackPanel;
import org.wickedsource.budgeteer.web.components.dataTable.DataTableBehavior;
import org.wickedsource.budgeteer.web.components.dataTable.editableMoneyField.EditableMoneyField;
import org.wickedsource.budgeteer.web.components.money.BudgetUnitMoneyModel;
import org.wickedsource.budgeteer.web.components.money.MoneyLabel;
import org.wickedsource.budgeteer.web.pages.hours.HoursPage;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

import static org.wicketstuff.lazymodel.LazyModel.from;
import static org.wicketstuff.lazymodel.LazyModel.model;

public class BurnTable extends Panel {

    private CustomFeedbackPanel feedbackPanel;
    private boolean dailyRateIsEditable;
    private DataView <WorkRecord> rows;
    private PagingNavigator pager;
    private Label pageLabel;
    WebMarkupContainer table;

    private Model<Long> recordsPerPageModel = new Model<Long>(15L);

    @Inject
    private RecordService recordService;

    public BurnTable(String id, FilteredRecordsModel model){
        this(id, model, false);
    }

    public BurnTable(String id, FilteredRecordsModel model, boolean dailyRateIsEditable) {
        super(id, model);
        this.dailyRateIsEditable = dailyRateIsEditable;

        feedbackPanel = new CustomFeedbackPanel("feedback");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        table = new WebMarkupContainer("table");
        HashMap<String, String> options = DataTableBehavior.getRecommendedOptions();
        options.put("orderClasses", "false");
        options.put("paging", "false");
        options.put("deferRendering", "true");
        options.put("info", "false");
        table.add(new DataTableBehavior(options));
        rows = createList("recordList", model, table);
        pager = new PagingNavigator("pager", rows);
        this.add(pager);
        table.add(rows);
        pageLabel = new Label("pageLabel", "Showing " + Long.toString(rows.getFirstItemOffset()+1) + " to "
                + Long.toString(rows.getFirstItemOffset() + rows.getItemsPerPage()) + " entries from total " + getRows().getItemCount());
        pageLabel.setOutputMarkupId(true);
        add(pageLabel);
        add(table);
        add(new MoneyLabel("total", new BudgetUnitMoneyModel(new TotalBudgetModel(model))));

        Form form = new Form("itemsPerPageForm") {
            @Override
            protected void onSubmit() {
                getRows().setItemsPerPage(recordsPerPageModel.getObject());
            }
        };
        form.add(new NumberTextField<>("itemsPerPage", recordsPerPageModel).setMinimum(1L).setMaximum(getRows().getItemCount()));
        add(form);
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        if(rows.getCurrentPage() == rows.getPageCount()-1){
            pageLabel.setDefaultModelObject("Showing " + Long.toString(rows.getFirstItemOffset()+1) + " to "
                    + Long.toString(rows.getFirstItemOffset() + (rows.getItemCount() % rows.getItemsPerPage())) + " entries from total " + getRows().getItemCount());
        }else{
            pageLabel.setDefaultModelObject("Showing " + Long.toString(rows.getFirstItemOffset()+1) + " to "
                    + Long.toString(rows.getFirstItemOffset() + rows.getItemsPerPage()) + " entries from total " + getRows().getItemCount());
        }
    }

    @Override
    public void onEvent(IEvent<?> event) {
        super.onEvent(event);
        Object payload = event.getPayload();
        if (payload instanceof WorkRecordFilter) {
            WorkRecordFilter filter = (WorkRecordFilter) payload;
            FilteredRecordsModel model = (FilteredRecordsModel) getDefaultModel();
            model.setFilter(filter);
        }
    }

    private DataView <WorkRecord> createList(String id, final FilteredRecordsModel model, final WebMarkupContainer table) {
        return new DataView<WorkRecord>(id, new ListDataProvider<WorkRecord>(model.getObject()){
            @Override
            protected List<WorkRecord> getData() {
                return model.getObject();
            }
        },recordsPerPageModel.getObject()) {

            @Override
            protected void populateItem(Item<WorkRecord> item) {
                item.setOutputMarkupId(true);
                item.add(new Label("budget", model(from(item.getModel()).getBudgetName())));
                item.add(new Label("person", model(from(item.getModel()).getPersonName()) ));
                if(dailyRateIsEditable) {
                    final EditableMoneyField editableMoneyField = new EditableMoneyField("dailyRate", table, model(from(item.getModelObject()).getDailyRate())) {
                        @Override
                        protected void save(AjaxRequestTarget target, Form<Money> form) {
                            item.getModelObject().setEditedManually(true);
                            item.getModelObject().setDailyRate(form.getModelObject());
                            recordService.saveDailyRateForWorkRecord(item.getModelObject());
                            target.add(item);
                        }

                        @Override
                        protected void cancel(AjaxRequestTarget target, Form<Money> form) {
                            item.getModelObject().setEditedManually(item.getModelObject().isEditedManually());
                            target.add(item);
                        }

                        @Override
                        protected void convertError(AjaxRequestTarget target) {
                            target.add(feedbackPanel);
                        }
                    };
                    item.add(editableMoneyField);
                } else {
                    item.add(new MoneyLabel("dailyRate", model(from(item.getModel()).getDailyRate())));
                }
                item.add(new Label("edited"){
                    @Override
                    public boolean isVisible() {
                        return item.getModelObject().isEditedManually();
                    }
                });
                item.add(new Label("date", model(from(item.getModel()).getDate())));
                item.add(new Label("hours", model(from(item.getModel()).getHours())));
                item.add(new MoneyLabel("burnedBudget", new BudgetUnitMoneyModel(model(from(item.getModel()).getBudgetBurned()))));
            }
        };
    }

    public DataView<WorkRecord> getRows() {
        return rows;
    }
}
