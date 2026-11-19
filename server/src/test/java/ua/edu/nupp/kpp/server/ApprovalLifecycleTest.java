package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApprovalLifecycleTest {

    @Test
    void draftCanBeSubmitted() {
        assertTrue(ApprovalRequestService.isAllowedTransition(ApprovalState.DRAFT, ApprovalState.SUBMITTED));
    }

    @Test
    void submittedCanGoToReview() {
        assertTrue(ApprovalRequestService.isAllowedTransition(ApprovalState.SUBMITTED, ApprovalState.REVIEW));
    }

    @Test
    void reviewCanBeApproved() {
        assertTrue(ApprovalRequestService.isAllowedTransition(ApprovalState.REVIEW, ApprovalState.APPROVED));
    }

    @Test
    void draftCannotSkipDirectlyToApproved() {
        assertFalse(ApprovalRequestService.isAllowedTransition(ApprovalState.DRAFT, ApprovalState.APPROVED));
        assertFalse(ApprovalRequestService.isAllowedTransition(ApprovalState.DRAFT, ApprovalState.REVIEW));
    }

    @Test
    void backwardAndRepeatedTransitionsAreForbidden() {
        assertFalse(ApprovalRequestService.isAllowedTransition(ApprovalState.SUBMITTED, ApprovalState.DRAFT));
        assertFalse(ApprovalRequestService.isAllowedTransition(ApprovalState.REVIEW, ApprovalState.SUBMITTED));
        assertFalse(ApprovalRequestService.isAllowedTransition(ApprovalState.APPROVED, ApprovalState.REVIEW));
        assertFalse(ApprovalRequestService.isAllowedTransition(ApprovalState.REVIEW, ApprovalState.REVIEW));
    }

    @Test
    void approvedStateIsFinal() {
        for (ApprovalState next : ApprovalState.values()) {
            assertFalse(ApprovalRequestService.isAllowedTransition(ApprovalState.APPROVED, next));
        }
    }

    @Test
    void exactlyThreeOfSixteenStatePairsAreAllowed() {
        int allowed = 0;
        for (ApprovalState from : ApprovalState.values()) {
            for (ApprovalState to : ApprovalState.values()) {
                if (ApprovalRequestService.isAllowedTransition(from, to)) {
                    allowed++;
                }
            }
        }
        assertEquals(3, allowed);
    }
}
