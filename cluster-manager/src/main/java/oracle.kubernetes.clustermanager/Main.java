// Copyright 2019, Oracle Corporation and/or its affiliates. All rights reserved.

package oracle.kubernetes.clustermanager;

import com.oracle.bmc.ClientConfiguration;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/** Cluster manager lets you get an ephemeral Kubernetes cluster for testing the operator. */
public class Main {

  public static final String CONFIG_FILE_PATH = "/home/mark/.oci/config.weblogick8s";
  public static final String PROFILE_NAME = "DEFAULT";
  public static final String NAMESPACE = "weblogick8s";
  public static final String BUCKET_NAME = "lease-bucket";
  public static final String OBJECT_NAME = "leases";
  private static ConfigFileReader.ConfigFile configWithProfile;
  private static Supplier<InputStream> privateKeySupplierFromConfigEntry;
  private static AuthenticationDetailsProvider provider;
  private static ClientConfiguration clientConfig;
  private static ObjectStorage client;

  public static void main(String[] args) {

    System.out.println("Cluster Manager");

    // Create the OCI API
    try {
      configWithProfile = ConfigFileReader.parse(CONFIG_FILE_PATH, PROFILE_NAME);
      privateKeySupplierFromConfigEntry =
          new SimplePrivateKeySupplier(configWithProfile.get("key_file"));
      provider = new ConfigFileAuthenticationDetailsProvider(CONFIG_FILE_PATH, PROFILE_NAME);

      clientConfig =
          ClientConfiguration.builder()
              .connectionTimeoutMillis(3000)
              .readTimeoutMillis(60000)
              .build();

      // do a quick sanity test
      client = new ObjectStorageClient(provider, clientConfig);
      client.setRegion(Region.US_PHOENIX_1);

    } catch (IOException e) {
      System.err.println("Could not read the OCI configuration file ~/.oci/config");
      System.exit(1);
    }

    GetObjectRequest getObjectRequest =
        GetObjectRequest.builder()
            .namespaceName(NAMESPACE)
            .bucketName(BUCKET_NAME)
            .objectName(OBJECT_NAME)
            .build();
    GetObjectResponse result = client.getObject(getObjectRequest);
    System.out.println("Got the lease object with e-tag: " + result.getETag());
  }
}