package com.csueb.exercise.saas;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

@WebServlet("/upload")
public class Upload extends HttpServlet {
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {

        Map<String, List<BlobKey>> blobs = blobstoreService.getUploads(req);
        List<BlobKey> blobKeys = blobs.get("myFile");

        if (blobKeys == null || blobKeys.isEmpty()) {
            res.sendRedirect("/");
        } else {
            
        	//res.sendRedirect("/serve?blob-key=" + blobKeys.get(0).getKeyString());
        		
        	byte[] blobBytes = getBlobBytes(blobKeys.get(0));
    		List<EntityAnnotation> imageLabels = getImageLabels(blobBytes);
    		
    		String imageUrl = getUploadedFileUrl(blobKeys.get(0));

    		
    		req.setAttribute("imageUrl", imageUrl);
    		req.setAttribute("imageLabels", imageLabels);
    		
    		RequestDispatcher dispatcher = getServletContext()
    			      .getRequestDispatcher("/labels.jsp");
    			    dispatcher.forward(req, res);	
        }
    }
    
    private String getUploadedFileUrl(BlobKey blobKey){
		ImagesService imagesService = ImagesServiceFactory.getImagesService();
		ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);
		return imagesService.getServingUrl(options);
	}
    
    
    private List<EntityAnnotation> getImageLabels(byte[] imgBytes) throws IOException {
		ByteString byteString = ByteString.copyFrom(imgBytes);
		Image image = Image.newBuilder().setContent(byteString).build();

		Feature feature = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
		AnnotateImageRequest request =
				AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
		List<AnnotateImageRequest> requests = new ArrayList<>();
		requests.add(request);

		ImageAnnotatorClient client = ImageAnnotatorClient.create();
		BatchAnnotateImagesResponse batchResponse = client.batchAnnotateImages(requests);
		client.close();
		List<AnnotateImageResponse> imageResponses = batchResponse.getResponsesList();
		AnnotateImageResponse imageResponse = imageResponses.get(0);

		if (imageResponse.hasError()) {
			System.err.println("Error getting image labels: " + imageResponse.getError().getMessage());
			return null;
		}
		
		return imageResponse.getLabelAnnotationsList();
	}
    
    
    private byte[] getBlobBytes(BlobKey blobKey) throws IOException {
		BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
		ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();

		int fetchSize = BlobstoreService.MAX_BLOB_FETCH_SIZE;
		long currentByteIndex = 0;
		boolean continueReading = true;
		while (continueReading) {
			// end index is inclusive, so we have to subtract 1 to get fetchSize bytes
			byte[] b = blobstoreService.fetchData(blobKey, currentByteIndex, currentByteIndex + fetchSize - 1);
			outputBytes.write(b);

			// if we read fewer bytes than we requested, then we reached the end
			if (b.length < fetchSize) {
				continueReading = false;
			}

			currentByteIndex += fetchSize;
		}

		return outputBytes.toByteArray();
	}
    
}