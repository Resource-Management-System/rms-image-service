package callum.project.imageService.exception;

import lombok.Getter;

@Getter
public class ServiceException extends Exception {

    private String message;

    public ServiceException(String message){
        this.message = message;
    }
}
