package org.wickedsource.budgeteer.web;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.wicket.Application;
import org.apache.wicket.DefaultExceptionMapper;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.springframework.security.access.AccessDeniedException;
import org.wickedsource.budgeteer.web.pages.dashboard.DashboardPage;

/**
 * A {@link DefaultExceptionMapper} that handles {@link AccessDeniedException AccessDeniedExceptions} via it's
 * {@link DefaultExceptionMapper#mapUnexpectedExceptions(Exception, Application)} method.
 */
public class BudgeteerExceptionMapper extends DefaultExceptionMapper {

    @Override
    protected IRequestHandler mapUnexpectedExceptions(Exception e, Application application) {
        Throwable cause = ExceptionUtils.getRootCause(e);

        if(cause instanceof AccessDeniedException) {
            return this.handleAccessDenied(e);
        } else {
            return super.mapUnexpectedExceptions(e, application);
        }
    }

    /**
     *
     * @param ex
     *          The thrown exception.
     * @return
     *          A {@link RenderPageRequestHandler} that redirects the user
     *          to the {@link DashboardPage}.
     */
    private IRequestHandler handleAccessDenied(Exception ex) {
        return new RenderPageRequestHandler(new PageProvider(DashboardPage.class));
    }

}
