package com.cloudproject2.lambda.license;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LicenseTextExtracter implements RequestHandler<GatewayRequest, GatewayResponse> {

    // Regex Patterns
    private static final String FIRST_NAME_PATTERN = "(FN|1)[ :]*([a-zA-Z ]+)";
    private static final String LAST_NAME_PATTERN = "(LN|2)[ :]*([a-zA-Z ]+)";
    private static final String DL_PATTERN = "(4d LIC NO|4d DLN|4d DL|LIC|DL|4D|dl|ID)[ :]+([\\w-]+)";
    private static final String EXP_PATTERN = "(EXP|Exp|4b|4B).+?(\\d{2}[-\\/]\\d{2}[-\\/]\\d{4})";

    @Override
    public GatewayResponse handleRequest(GatewayRequest request, Context context) {
        System.out.println("Inside lambda + " + request.getFileName());
        AmazonRekognition amazonRekognition = AmazonRekognitionClientBuilder.defaultClient();
        UserLicense userlicense = new UserLicense();
        DetectTextRequest detectTextRequest = new DetectTextRequest()
                .withImage(new Image()
                .withS3Object(new S3Object()
                .withName(request.getFileName())
                .withBucket("mssecarrental")));

        try {
            Pattern fnPattern = Pattern.compile(FIRST_NAME_PATTERN);
            Pattern lnPattern = Pattern.compile(LAST_NAME_PATTERN);
            Pattern dlPattern = Pattern.compile(DL_PATTERN);
            Pattern expPattern = Pattern.compile(EXP_PATTERN);

            DetectTextResult result = amazonRekognition.detectText(detectTextRequest);
            List<TextDetection> textDetections = result.getTextDetections();

            for (int index = 0; index < textDetections.size() - 1; index++) {

                // Match Only LINES but not words
                if (textDetections.get(index).getType().equalsIgnoreCase("LINE")) {
                    String currentTextString = textDetections.get(index).getDetectedText();

                    Matcher dlMatcher = dlPattern.matcher(currentTextString);
                    if (dlMatcher.find()) {
                        userlicense.setLicense(dlMatcher.group(2));
                        continue;
                    }

                    Matcher expMatcher = expPattern.matcher(currentTextString);
                    if (expMatcher.find()) {
                        userlicense.setExpiryDate(convertToDate(expMatcher.group(2)));
                        continue;
                    }

                    Matcher fnMatcher = fnPattern.matcher(currentTextString);
                    if (fnMatcher.find()) {
                        userlicense.setFirstname(fnMatcher.group(2));
                        continue;
                    }

                    Matcher lnMatcher = lnPattern.matcher(currentTextString);
                    if (lnMatcher.find()) {
                        userlicense.setLastname(lnMatcher.group(2));
                        continue;
                    }

                    if (userlicense.isComplete()) {
                        break;
                    }
                }
            }

            // Success case return back 200
            return new GatewayResponse()
                    .setBody(userlicense)
                    .setStatusCode(200);
        } catch (AmazonRekognitionException e) {
            e.printStackTrace();
        }

        // Error case 404
        return new GatewayResponse()
                .setStatusCode(404);
    }

    private Date convertToDate(String textString) {
        SimpleDateFormat formatter = new SimpleDateFormat(textString.contains("/") ? "MM/dd/yyyy" : "MM-dd-yyyy");
        try {
            return formatter.parse(textString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

}
