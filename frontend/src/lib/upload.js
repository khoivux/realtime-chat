// Upload function that calls backend API

const upload = async (file) => {
  try {
    // Create FormData to send file
    const formData = new FormData();
    formData.append('file', file);
    
    // Get token from localStorage for authentication
    const token = localStorage.getItem('accessToken');
    
    // Call upload API
    const response = await fetch('http://localhost:8081/api/v1/upload/', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    });
    
    if (!response.ok) {
      throw new Error(`Upload failed: ${response.status} ${response.statusText}`);
    }
    
    const result = await response.json();
    console.log('Upload API Response:', result);
    console.log('Upload API Response structure:', JSON.stringify(result, null, 2));
    
    // Check if API response is successful
    if (result.code === 1000 && result.data) {
      console.log('Upload successful, returning data:', result.data);
      // Make sure we return the URL string, not an object
      if (typeof result.data === 'string') {
        return result.data; // Return the uploaded file URL string
      } else if (result.data.data && typeof result.data.data === 'string') {
        return result.data.data; // Handle nested data structure
      } else {
        console.error('Unexpected data structure:', result.data);
        return result.data; // Return as-is and hope for the best
      }
    } 
    // Fallback: if response doesn't have expected structure but is successful
    else if (response.ok && result) {
      console.log('Upload successful, using fallback, returning full result:', result);
      // For fallback, also try to extract string URL
      if (typeof result === 'string') {
        return result;
      } else if (result.data && typeof result.data === 'string') {
        return result.data;
      }
      return result; // Return the full result as fallback
    } 
    else {
      console.error('Upload API error:', result);
      throw new Error(result.message || 'Upload failed');
    }
    
  } catch (error) {
    console.error('Upload error:', error);
    throw new Error(`File upload failed: ${error.message}`);
  }
};

export default upload;
