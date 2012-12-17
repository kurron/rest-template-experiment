package org.kurron.rest.org.kurron.rest;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.Random;

/**
 * A simple learning test to see how Spring's RestTemplate deals with large HTTP uploads.
 */
public class RestTemplateExercise {

    public static void main( final String[] args ) throws Exception
    {
        if ( 0 == args.length )
        {
            System.err.println("You need to specify the relative path to your file to upload.");
        }
        else
        {
/*
            final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setBufferRequestBody( false );
            final int chunkSize = 1024 * 8;
            factory.setChunkSize(chunkSize);
            System.out.println( "Using a streaming POST with a chunk size of " + chunkSize + " bytes." );
*/

            final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
            RestOperations template = new RestTemplate(factory);
            URI uri = UriComponentsBuilder.newInstance().scheme( "http" ).host("192.168.254.47").port(80).path("LessonGin/publish.pl").build().toUri();
            final String file = args[0];
            Resource resource = new FileSystemResource( file );
            assert resource.exists();
            if ( resource.exists() )
            {
                System.out.println("About to send " + file + " which is " + resource.contentLength() + " bytes long.");

                final HttpHeaders headers = new HttpHeaders();
                headers.add( "operation", "put" );
                headers.add( "directory", generateRandomDirectoryName() );
                headers.add( "filename", generateRandomFileName() );

                HttpEntity<Resource> request = new HttpEntity<>( resource, headers );
                System.out.println( "Start "+ Calendar.getInstance().getTime().toString() );
                URI location = template.postForLocation( uri, request );
                System.out.println( "Stop "+ Calendar.getInstance().getTime().toString() );

                assert location != null;
                System.out.println( "location = " + location );
            }
            else
            {
                System.err.println("File " + file + " could not be found. Please double check the path.");
            }
        }
    }

    private static final Random random = new Random();

    private static String generateRandomFileName() {
        return Integer.toHexString( random.nextInt( Integer.MAX_VALUE ) ).toUpperCase() + ".bin";
    }

    private static String generateRandomDirectoryName() {
        return Integer.toHexString( random.nextInt( Integer.MAX_VALUE ) ).toUpperCase();
    }
}
