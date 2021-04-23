package eu.h2020.helios_social.happ.helios.talk.account;

import eu.h2020.helios_social.modules.groupcommunications_utils.account.AccountManager;
import eu.h2020.helios_social.modules.groupcommunications_utils.crypto.PasswordStrengthEstimator;
import eu.h2020.helios_social.modules.groupcommunications_utils.lifecycle.IoExecutor;
import eu.h2020.helios_social.modules.groupcommunications_utils.nullsafety.NotNullByDefault;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.ResultHandler;
import eu.h2020.helios_social.happ.helios.talk.controller.handler.UiResultHandler;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.inject.Inject;

import androidx.annotation.Nullable;

import eu.h2020.helios_social.modules.groupcommunications.context.ContextManager;

@NotNullByDefault
public class SetupControllerImpl implements SetupController {

    private static final Logger LOG =
            Logger.getLogger(SetupControllerImpl.class.getName());

    private final AccountManager accountManager;
    private final PasswordStrengthEstimator strengthEstimator;
    private final ContextManager contextManager;
    @IoExecutor
    private final Executor ioExecutor;
    @Nullable
    private volatile SetupActivity setupActivity;

    @Inject
    SetupControllerImpl(AccountManager accountManager,
                        @IoExecutor Executor ioExecutor,
                        PasswordStrengthEstimator strengthEstimator,
                        ContextManager contextManager) {
        this.accountManager = accountManager;
        this.strengthEstimator = strengthEstimator;
        this.ioExecutor = ioExecutor;
        this.contextManager = contextManager;
    }

    @Override
    public void setSetupActivity(SetupActivity setupActivity) {
        this.setupActivity = setupActivity;
    }

    @Override
    public boolean needToShowDozeFragment() {
        SetupActivity setupActivity = this.setupActivity;
        if (setupActivity == null) throw new IllegalStateException();
        return DozeView.needsToBeShown(setupActivity) ||
                HuaweiView.needsToBeShown(setupActivity);
    }

    @Override
    public void setAuthorName(String authorName) {
        SetupActivity setupActivity = this.setupActivity;
        if (setupActivity == null) throw new IllegalStateException();
        setupActivity.setAuthorName(authorName);
    }

    @Override
    public float estimatePasswordStrength(String password) {
        return strengthEstimator.estimateStrength(password);
    }

    @Override
    public void setPassword(String password) {
        SetupActivity setupActivity = this.setupActivity;
        if (setupActivity == null) throw new IllegalStateException();
        setupActivity.setPassword(password);
    }

    @Override
    public void showAuthorNameFragment() {
        SetupActivity setupActivity = this.setupActivity;
        if (setupActivity == null) throw new IllegalStateException();
        setupActivity.showAuthorNameFragment();
    }

    @Override
    public void showOnBoardingStepTwo() {
        SetupActivity setupActivity = this.setupActivity;
        if (setupActivity == null) throw new IllegalStateException();
        setupActivity.showOnBoardingFragmentStepTwo();
    }

    @Override
    public void showOnBoardingStepThree() {
        SetupActivity setupActivity = this.setupActivity;
        if (setupActivity == null) throw new IllegalStateException();
        setupActivity.showOnBoardingFragmentStepThree();
    }

    @Override
    public void showOnBoardingStepFour() {
        SetupActivity setupActivity = this.setupActivity;
        if (setupActivity == null) throw new IllegalStateException();
        setupActivity.showOnBoardingFragmentStepFour();
    }

    @Override
    public void showOnBoardingStepFive() {
        SetupActivity setupActivity = this.setupActivity;
        if (setupActivity == null) throw new IllegalStateException();
        setupActivity.showOnBoardingFragmentStepFive();
    }

    @Override
    public void showPasswordFragment() {
        SetupActivity setupActivity = this.setupActivity;
        if (setupActivity == null) throw new IllegalStateException();
        setupActivity.showPasswordFragment();
    }

    @Override
    public void showDozeFragment() {
        SetupActivity setupActivity = this.setupActivity;
        if (setupActivity == null) throw new IllegalStateException();
        setupActivity.showDozeFragment();
    }

    @Override
    public void createAccount() {
        SetupActivity setupActivity = this.setupActivity;
        UiResultHandler<Boolean> resultHandler =
                new UiResultHandler<Boolean>(setupActivity) {
                    @Override
                    public void onResultUi(Boolean result) {
                        // TODO: Show an error if result is false
                        if (setupActivity == null)
                            throw new IllegalStateException();
                        setupActivity.showProfileSetup();
                    }
                };
        createAccount(resultHandler);
    }

    // Package access for testing
    void createAccount(ResultHandler<Boolean> resultHandler) {
        SetupActivity setupActivity = this.setupActivity;
        if (setupActivity == null) throw new IllegalStateException();
        String authorName = setupActivity.getAuthorName();
        if (authorName == null) throw new IllegalStateException();
        String password = setupActivity.getPassword();
        if (password == null) throw new IllegalStateException();
        ioExecutor.execute(() -> {
            LOG.info("Creating account");
            resultHandler.onResult(accountManager.createAccount(authorName,
                    password));
        });
    }
}
