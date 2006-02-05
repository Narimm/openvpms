package org.openvpms.web.app.financial;

import org.openvpms.web.app.subsystem.DummyWorkspace;
import org.openvpms.web.component.subsystem.AbstractSubsystem;


/**
 * Financial subsystem.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public class FinancialSubsystem extends AbstractSubsystem {

    public FinancialSubsystem() {
        super("financial");
        addWorkspace(new DummyWorkspace("financial.till"));
        addWorkspace(new DummyWorkspace("financial.deposit"));
        addWorkspace(new DummyWorkspace("financial.period"));
        addWorkspace(new DummyWorkspace("financial.tax"));
    }
}
