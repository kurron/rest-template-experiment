package org.kurron.rest.org.kurron.rest;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * A simple learning test to see how Spring's RestTemplate deals with large HTTP uploads.
 */
public class RestTemplateExercise {

    private static class ChunkableResource extends FileSystemResource
    {
        private ChunkableResource(File file) {
            super(file);
        }

        private ChunkableResource(String path) {
            super(path);
        }

        @Override
        public long contentLength() throws IOException {
            return -1;
        }
    }
    public static void main( final String[] args ) throws Exception
    {
        if ( 0 == args.length )
        {
            System.err.println("You need to specify the relative path to your file to upload.");
        }
        else
        {
            final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setBufferRequestBody( false );
            final int chunks;
            if ( 1 == args.length )
            {
                chunks = 8;
            }
            else
            {
                chunks = Integer.parseInt( args[1] );
            }
            final int chunkSize = 1024 * chunks;
            factory.setChunkSize( chunkSize );
            System.out.println( "Using a streaming POST with a chunk size of " + chunkSize + " bytes." );

            //final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

            RestOperations template = new RestTemplate(factory);
            URI uri = UriComponentsBuilder.newInstance().scheme( "http" ).host("192.168.254.47").port(80).path("LessonGin/publish.pl").build().toUri();
            final String file = args[0];
            Resource resource = new FileSystemResource( file );
            assert resource.exists();
            if ( resource.exists() )
            {
                System.out.println("About to send " + file + " which is " + resource.contentLength() + " bytes long.");

                final HttpHeaders headers = new HttpHeaders();
                headers.setContentLength( resource.contentLength() );
                headers.setContentType( MediaType.APPLICATION_OCTET_STREAM );
                headers.add( "operation", "put" );
                headers.add( "directory", generateRandomDirectoryName() );
                headers.add( "filename", generateRandomFileName() );

                HttpEntity<Resource> request = new HttpEntity<>( resource, headers );
                final Date start = Calendar.getInstance().getTime();
                System.out.println("Start " + start.toString());
                URI location = template.postForLocation( uri, request );
                final Date stop = Calendar.getInstance().getTime();
                System.out.println("Stop " + stop.toString());
                long duration = stop.getTime() - start.getTime();
                System.out.println( "Duration: " + duration/1000 + " seconds" );

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
