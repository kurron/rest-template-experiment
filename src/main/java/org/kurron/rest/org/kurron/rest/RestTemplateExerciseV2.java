package org.kurron.rest.org.kurron.rest;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.*;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.*;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A simple learning test to see how Spring's RestTemplate deals with large HTTP downloads.
 */
public class RestTemplateExerciseV2 {

    public static void copyBufferedStream( final InputStream in, final OutputStream out ) throws IOException
    {
        final int bufferSize = 1024 * 8;
        System.out.println( "bufferSize = " + bufferSize );
        final BufferedInputStream bufferedIn = new BufferedInputStream( in, bufferSize );
        final BufferedOutputStream bufferedOut = new BufferedOutputStream( out, bufferSize );
        while( true )
        {
            final int datum = bufferedIn.read();
            if( -1 == datum )
            {
                break;
            }
            bufferedOut.write( datum );
        }
        /*
        The output stream is deliberately flushed. The data reaches its eventual destination
        in the underlying stream only when the stream is flushed or the buffer fills up.
        Therefore, it’s important to call flush( ) explicitly before the method returns.
        */
        bufferedOut.flush();
        closeStream( in, out );
    }

    public static void copyStream( final InputStream in, final OutputStream out ) throws IOException
    {
        final int bufferSize = 1024 * 32;
        System.out.println( "bufferSize = " + bufferSize );
        final byte[] buffer = new byte[bufferSize];
        while( true )
        {
            final int bytesRead = in.read( buffer );
            if( -1 == bytesRead )
            {
                break;
            }
            out.write( buffer, 0, bytesRead );
        }
        closeStream( in, out );
    }

    public static void closeStream( final Closeable... stream )
    {
        for( final Closeable closeable : stream )
        {
            if( null != closeable )
            {
                try
                {
                    closeable.close();
                }
                catch( final IOException e )
                {
                    // nothing we can do so eat the exception
                }
            }
        }
    }

    static class ZipResourceHttpMessageConverter extends ResourceHttpMessageConverter
    {
        @Override
        protected Resource readInternal(Class<? extends Resource> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
            final FileSystemResource resource = new FileSystemResource( "temp-data.bin" );
            //FileCopyUtils.copy( inputMessage.getBody(), resource.getOutputStream() );
            // copyBufferedStream( inputMessage.getBody(), resource.getOutputStream() );
            copyStream( inputMessage.getBody(), resource.getOutputStream() );
            return  resource;
        }

        @Override
        protected boolean canRead(MediaType mediaType) {
            final boolean matches;
            if ( null == mediaType )
            {
                matches = false;
            }
            else
            {
                matches = mediaType.equals(MediaType.valueOf("application/zip"));
                if ( matches )
                {
                    System.out.println( "Matches!" );
                }
            }
            return matches;
        }
    }

    public static void main( final String[] args ) throws Exception
    {
        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setBufferRequestBody( false );
        final int chunkSize = 1024 * 8;
        factory.setChunkSize( chunkSize );
        System.out.println( "Using a streaming POST with a chunk size of " + chunkSize + " bytes." );

        final RestTemplate template = new RestTemplate(factory);
        final List<HttpMessageConverter<?>> messageConverters = template.getMessageConverters();
        messageConverters.add( 0, new ZipResourceHttpMessageConverter() );
        //final URI uri = UriComponentsBuilder.newInstance().scheme( "http" ).host("192.168.254.47").port(80).path("LessonGin/ted/250mb.bin").build().toUri();
        final URI uri = UriComponentsBuilder.newInstance().scheme( "http" ).host("iteration").port(8080).path("CW_PIF.zip").build().toUri();
        System.out.println("GETing ");
        final Date start = Calendar.getInstance().getTime();
        final ResponseEntity<FileSystemResource> entity = template.getForEntity(uri, FileSystemResource.class );
        final Date stop = Calendar.getInstance().getTime();
        System.out.println("Stop " + stop.toString());
        long duration = stop.getTime() - start.getTime();
        System.out.println( "Duration: " + duration/1000 + " seconds" );
        assert null != entity;
        final FileSystemResource body = entity.getBody();
        FileCopyUtils.copy( body.getInputStream(), new FileOutputStream( "save-to-disk.bin" ) );
    }
}
