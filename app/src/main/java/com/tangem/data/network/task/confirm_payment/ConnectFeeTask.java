package com.tangem.data.network.task.confirm_payment;

import android.app.Activity;
import android.view.View;

import com.tangem.data.network.request.FeeRequest;
import com.tangem.data.network.task.FeeTask;
import com.tangem.domain.wallet.SharedData;
import com.tangem.presentation.activity.ConfirmPaymentActivity;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;

public class ConnectFeeTask extends FeeTask {
    private WeakReference<ConfirmPaymentActivity> reference;

    public ConnectFeeTask(ConfirmPaymentActivity context, SharedData sharedData) {
        super(sharedData);
        reference = new WeakReference<>(context);
    }

    @Override
    protected void onPostExecute(List<FeeRequest> requests) {
        super.onPostExecute(requests);
        ConfirmPaymentActivity confirmPaymentActivity = reference.get();

        for (FeeRequest request : requests) {
            if (request.error == null) {
                long minFeeRate = 0;
                try {
                    try {
                        String tmpAnswer = request.getAsString();
                        BigDecimal minFeeBD = new BigDecimal(tmpAnswer);
                        BigDecimal multiplicator = new BigDecimal("100000000");
                        minFeeBD = minFeeBD.multiply(multiplicator);
                        BigInteger minFeeBI = minFeeBD.toBigInteger();
                        minFeeRate = minFeeBI.longValue();
                    } catch (Exception e) {

                        if (sharedCounter != null) {
                            int errCounter = sharedCounter.errorRequest.incrementAndGet();


                            if (errCounter >= sharedCounter.allRequest) {
                                confirmPaymentActivity.progressBar.setVisibility(View.INVISIBLE);
                                confirmPaymentActivity.finishActivityWithError(Activity.RESULT_CANCELED, "Cannot calculate fee! No connection with blockchain nodes");
                            }
                        } else {
                            confirmPaymentActivity.progressBar.setVisibility(View.INVISIBLE);
                            confirmPaymentActivity.finishActivityWithError(Activity.RESULT_CANCELED, "Cannot calculate fee! No connection with blockchain nodes");
                        }

                        //FinishActivityWithError(Activity.RESULT_CANCELED, "Cannot calculate fee! No connection with blockchain nodes");
                        return;
                    }

                    if (minFeeRate == 0) {
                        confirmPaymentActivity.progressBar.setVisibility(View.INVISIBLE);
                        confirmPaymentActivity.finishActivityWithError(Activity.RESULT_CANCELED, "Cannot calculate fee! Wrong data received from the node");
                        return;
                    }

                    long inputCount = request.txSize;

                    if (inputCount != 0) {
                        minFeeRate = minFeeRate * inputCount;
                    } else {
                        minFeeRate = minFeeRate * 256;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (sharedCounter != null) {
                        int errCounter = sharedCounter.errorRequest.incrementAndGet();
                        if (errCounter >= sharedCounter.allRequest) {
                            confirmPaymentActivity.progressBar.setVisibility(View.INVISIBLE);
                            confirmPaymentActivity.finishActivityWithError(Activity.RESULT_CANCELED, "Cannot calculate fee! No connection with blockchain nodes");
                        }
                    } else {
                        confirmPaymentActivity.progressBar.setVisibility(View.INVISIBLE);
                        confirmPaymentActivity.finishActivityWithError(Activity.RESULT_CANCELED, "Cannot calculate fee! No connection with blockchain nodes");
                    }
                    return;
                }

                confirmPaymentActivity.progressBar.setVisibility(View.INVISIBLE);

                float finalFee = (float) minFeeRate / (float) 10000;

                finalFee = Math.round(finalFee) / (float) 10000;

                if ((request.getBlockCount() == FeeRequest.MINIMAL) && (confirmPaymentActivity.minFee == null)) {
                    confirmPaymentActivity.minFee = String.valueOf(finalFee);
                    confirmPaymentActivity.minFeeInInternalUnits = confirmPaymentActivity.mCard.InternalUnitsFromString(String.valueOf(finalFee));
                } else if ((request.getBlockCount() == FeeRequest.NORMAL) && (confirmPaymentActivity.normalFee == null)) {
                    confirmPaymentActivity.normalFee = String.valueOf(finalFee);
                } else if ((request.getBlockCount() == FeeRequest.PRIORITY) && (confirmPaymentActivity.maxFee == null)) {
                    confirmPaymentActivity.maxFee = String.valueOf(finalFee);
                }

                confirmPaymentActivity.doSetFee(confirmPaymentActivity.rgFee.getCheckedRadioButtonId());

                confirmPaymentActivity.etFee.setError(null);
                confirmPaymentActivity.feeRequestSuccess = true;
                if (confirmPaymentActivity.feeRequestSuccess && confirmPaymentActivity.balanceRequestSuccess) {
                    confirmPaymentActivity.btnSend.setVisibility(View.VISIBLE);
                }
                confirmPaymentActivity.dtVerifyed = new Date();

            } else {

                if (sharedCounter != null) {
                    int errCounter = sharedCounter.errorRequest.incrementAndGet();
                    if (errCounter >= sharedCounter.allRequest) {
                        confirmPaymentActivity.progressBar.setVisibility(View.INVISIBLE);
                        confirmPaymentActivity.finishActivityWithError(Activity.RESULT_CANCELED, "Cannot calculate fee! No connection with blockchain nodes");
                    }
                } else {
                    confirmPaymentActivity.progressBar.setVisibility(View.INVISIBLE);
                    confirmPaymentActivity.finishActivityWithError(Activity.RESULT_CANCELED, "Cannot calculate fee! No connection with blockchain nodes");
                }
            }
        }
    }

}