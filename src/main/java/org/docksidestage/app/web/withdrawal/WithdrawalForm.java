package org.docksidestage.app.web.withdrawal;

import org.hibernate.validator.constraints.Length;

/**
 * @author annie_pocket
 * @author jflute
 */
public class WithdrawalForm {

    private String selectedReason;

    @Length(max = 3)
    private String reasonInput;

    public String getSelectedReason() {
        return selectedReason;
    }

    public void setSelectedReason(String selectedReason) {
        this.selectedReason = selectedReason;
    }

    public String getReasonInput() {
        return reasonInput;
    }

    public void setReasonInput(String reasonInput) {
        this.reasonInput = reasonInput;
    }

}
