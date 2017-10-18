package org.priere.web;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;
import org.priere.domain.ProgrammeEntity;
import org.priere.domain.ProgrammeImage;
import org.priere.domain.security.UserEntity;
import org.priere.service.ProgrammeService;
import org.priere.service.security.SecurityWrapper;
import org.priere.service.security.UserService;
import org.priere.web.generic.GenericLazyDataModel;
import org.priere.web.util.MessageFactory;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

@Named("programmeBean")
@ViewScoped
public class ProgrammeBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = Logger.getLogger(ProgrammeBean.class.getName());
    
    private GenericLazyDataModel<ProgrammeEntity> lazyModel;
    
    private ProgrammeEntity programme;
    
    @Inject
    private ProgrammeService programmeService;
    
    UploadedFile uploadedImage;
    byte[] uploadedImageContents;
    
    @Inject
    private UserService userService;
    
    private List<UserEntity> allOfficiantsList;
    
    private List<UserEntity> allPredicateursList;
    
    public void prepareNewProgramme() {
        reset();
        this.programme = new ProgrammeEntity();
        // set any default values now, if you need
        // Example: this.programme.setAnything("test");
    }

    public GenericLazyDataModel<ProgrammeEntity> getLazyModel() {
        if (this.lazyModel == null) {
            this.lazyModel = new GenericLazyDataModel<>(programmeService);
        }
        return this.lazyModel;
    }
    
    public String persist() {

        if (programme.getId() == null && !isPermitted("programme:create")) {
            return "accessDenied";
        } else if (programme.getId() != null && !isPermitted(programme, "programme:update")) {
            return "accessDenied";
        }
        
        String message;
        
        try {
            
            if (this.uploadedImage != null) {
                try {

                    BufferedImage image;
                    try (InputStream in = new ByteArrayInputStream(uploadedImageContents)) {
                        image = ImageIO.read(in);
                    }
                    image = Scalr.resize(image, Method.BALANCED, 300);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageOutputStream imageOS = ImageIO.createImageOutputStream(baos);
                    Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByMIMEType(
                            uploadedImage.getContentType());
                    ImageWriter imageWriter = (ImageWriter) imageWriters.next();
                    imageWriter.setOutput(imageOS);
                    imageWriter.write(image);
                    
                    baos.close();
                    imageOS.close();
                    
                    logger.log(Level.INFO, "Resized uploaded image from {0} to {1}", new Object[]{uploadedImageContents.length, baos.toByteArray().length});
            
                    ProgrammeImage programmeImage = new ProgrammeImage();
                    programmeImage.setContent(baos.toByteArray());
                    programme.setImage(programmeImage);
                } catch (Exception e) {
                    FacesMessage facesMessage = MessageFactory.getMessage(
                            "message_upload_exception");
                    FacesContext.getCurrentInstance().addMessage(null, facesMessage);
                    FacesContext.getCurrentInstance().validationFailed();
                    return null;
                }
            }
            
            if (programme.getId() != null) {
                programme = programmeService.update(programme);
                message = "message_successfully_updated";
            } else {
                programme = programmeService.save(programme);
                message = "message_successfully_created";
            }
        } catch (OptimisticLockException e) {
            logger.log(Level.SEVERE, "Error occured", e);
            message = "message_optimistic_locking_exception";
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        } catch (PersistenceException e) {
            logger.log(Level.SEVERE, "Error occured", e);
            message = "message_save_exception";
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        }
        
        FacesMessage facesMessage = MessageFactory.getMessage(message);
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        
        return null;
    }
    
    public String delete() {
        
        if (!isPermitted(programme, "programme:delete")) {
            return "accessDenied";
        }
        
        String message;
        
        try {
            programmeService.delete(programme);
            message = "message_successfully_deleted";
            reset();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error occured", e);
            message = "message_delete_exception";
            // Set validationFailed to keep the dialog open
            FacesContext.getCurrentInstance().validationFailed();
        }
        FacesContext.getCurrentInstance().addMessage(null, MessageFactory.getMessage(message));
        
        return null;
    }
    
    public void onDialogOpen(ProgrammeEntity programme) {
        reset();
        this.programme = programme;
    }
    
    public void reset() {
        programme = null;

        allOfficiantsList = null;
        
        allPredicateursList = null;
        
        uploadedImage = null;
        uploadedImageContents = null;
        
    }

    // Get a List of all officiant
    public List<UserEntity> getOfficiants() {
        if (this.allOfficiantsList == null) {
            this.allOfficiantsList = userService.findAllUserEntities();
        }
        return this.allOfficiantsList;
    }
    
    // Update officiant of the current programme
    public void updateOfficiant(UserEntity user) {
        this.programme.setOfficiant(user);
        // Maybe we just created and assigned a new user. So reset the allOfficiantList.
        allOfficiantsList = null;
    }
    
    // Get a List of all predicateur
    public List<UserEntity> getPredicateurs() {
        if (this.allPredicateursList == null) {
            this.allPredicateursList = userService.findAllUserEntities();
        }
        return this.allPredicateursList;
    }
    
    // Update predicateur of the current programme
    public void updatePredicateur(UserEntity user) {
        this.programme.setPredicateur(user);
        // Maybe we just created and assigned a new user. So reset the allPredicateurList.
        allPredicateursList = null;
    }
    
    public void handleImageUpload(FileUploadEvent event) {
        
        Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByMIMEType(
                event.getFile().getContentType());
        if (!imageWriters.hasNext()) {
            FacesMessage facesMessage = MessageFactory.getMessage(
                    "message_image_type_not_supported");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            return;
        }
        
        this.uploadedImage = event.getFile();
        this.uploadedImageContents = event.getFile().getContents();
        
        FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
        FacesContext.getCurrentInstance().addMessage(null, message);
    }
    
    public byte[] getUploadedImageContents() {
        if (uploadedImageContents != null) {
            return uploadedImageContents;
        } else if (programme != null && programme.getImage() != null) {
            programme = programmeService.lazilyLoadImageToProgramme(programme);
            return programme.getImage().getContent();
        }
        return null;
    }
    
    public ProgrammeEntity getProgramme() {
        if (this.programme == null) {
            prepareNewProgramme();
        }
        return this.programme;
    }
    
    public void setProgramme(ProgrammeEntity programme) {
        this.programme = programme;
    }
    
    public boolean isPermitted(String permission) {
        return SecurityWrapper.isPermitted(permission);
    }

    public boolean isPermitted(ProgrammeEntity programme, String permission) {
        
        return SecurityWrapper.isPermitted(permission);
        
    }
    
}
