package org.wickedsource.budgeteer.service.security.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.wickedsource.budgeteer.service.security.BudgeteerMethodSecurityExpressionHandler;
import org.wickedsource.budgeteer.service.security.BudgeteerMethodSecurityExpressionRoot;

/**
 * A configuration to set up custom spring boot security expressions.
 *
 * @see BudgeteerMethodSecurityExpressionHandler
 * @see BudgeteerMethodSecurityExpressionRoot
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {

    private final BudgeteerMethodSecurityExpressionHandler handler;

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return handler;
    }

}
