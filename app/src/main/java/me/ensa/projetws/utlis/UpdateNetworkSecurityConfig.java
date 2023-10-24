package me.ensa.projetws.utlis;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import org.xmlpull.v1.XmlPullParser;
import java.io.FileOutputStream;

import me.ensa.projetws.R;

public class UpdateNetworkSecurityConfig {
    public static void updateNetworkSecurityConfig(Context context) {
        String ipAddress = GetIPAddress.getIP();
        if (ipAddress != null) {
            try {
                // Open the XML file from res/xml directory
                Resources res = context.getResources();
                XmlResourceParser xrp = res.getXml(R.xml.network_security_config);

                // Read the XML content
                StringBuilder stringBuilder = new StringBuilder();
                int eventType = xrp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xrp.getName().equals("domain")) {
                            String domain = xrp.getAttributeValue(null, "includeSubdomains");
                            if (domain != null && domain.equals("true")) {
                                String currentIp = xrp.nextText();
                                if (currentIp != null) {
                                    xrp.next();
                                    // Update the IP address
                                    stringBuilder.append("<domain includeSubdomains=\"true\">")
                                            .append(ipAddress)
                                            .append("</domain>\n");
                                    continue;
                                }
                            }
                        }
                    }
                    stringBuilder.append(xrp.getText());
                    eventType = xrp.next();
                }

                // Close the XML parser
                xrp.close();

                // Write the updated content back to the file
                FileOutputStream outputStream = context.openFileOutput("network_security_config.xml", Context.MODE_PRIVATE);
                outputStream.write(stringBuilder.toString().getBytes());
                outputStream.close();

                System.out.println("Updated network-security-config.xml with IP: " + ipAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Failed to retrieve the IP address.");
        }
    }
}

