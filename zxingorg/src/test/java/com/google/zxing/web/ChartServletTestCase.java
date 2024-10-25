/*
 * Copyright 2020 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.web;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.File;  // Added to simulate file upload handling
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Tests {@link ChartServlet}.
 */
public final class ChartServletTestCase extends Assert {

  @Test
  public void testChart() throws Exception {
    ChartServlet servlet = new ChartServlet();

    for (String contentType : new String[] { "png", "jpeg", "gif"}) {
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setRequestURI("image." + contentType);
      Map<String, String> params = new HashMap<>();
      params.put("chl", "foo");
      params.put("chs", "100x100");
      params.put("chld", "M");
      request.setParameters(params);

      MockHttpServletResponse response = new MockHttpServletResponse();

      servlet.doGet(request, response);

      assertEquals(HttpServletResponse.SC_OK, response.getStatus());
      assertEquals("image/" + contentType, response.getContentType());
      assertTrue(response.getContentAsByteArray().length > 0);
    }

    servlet.destroy();
  }

  // Scenario 1: Insecure File Handling (Potential Directory Traversal)
  @Test
  public void testInsecureFileHandling() {
    try {
      String fileName = "../etc/passwd";  // Simulating user input that could lead to directory traversal
      File file = new File("/uploads/" + fileName);

      // Vulnerable to directory traversal if the fileName input isn't sanitized
      FileWriter writer = new FileWriter(file);
      writer.write("Some content");
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  // Scenario 2: Missing Rate Limiting (Denial of Service risk)
  @Test
  public void testNoRateLimiting() throws Exception {
    ChartServlet servlet = new ChartServlet();

    // Simulating multiple rapid requests without any rate limiting
    for (int i = 0; i < 1000; i++) {
      MockHttpServletRequest request = new MockHttpServletRequest();
      request.setRequestURI("image.png");
      Map<String, String> params = new HashMap<>();
      params.put("chl", "foo");
      params.put("chs", "100x100");
      params.put("chld", "M");
      request.setParameters(params);

      MockHttpServletResponse response = new MockHttpServletResponse();
      servlet.doGet(request, response);

      assertEquals(HttpServletResponse.SC_OK, response.getStatus());
      assertTrue(response.getContentAsByteArray().length > 0);
    }

    servlet.destroy();
  }

}
