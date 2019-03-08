package finalproject.emag.controller;

import finalproject.emag.model.pojo.messages.ErrorMsg;
import finalproject.emag.model.pojo.User;
import finalproject.emag.util.exception.BaseException;
import finalproject.emag.util.exception.NotAdminException;
import finalproject.emag.util.exception.NotLoggedException;
import finalproject.emag.util.exception.WrongCredentialsException;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.time.LocalDateTime;

@RestController
public abstract class BaseController {

    private static Logger log = Logger.getLogger(BaseController.class.getName());

    @ExceptionHandler({NotLoggedException.class, NotAdminException.class})
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    public ErrorMsg handleNotLogged(Exception e){
        log.error("exception: "+e);
        return new ErrorMsg(e.getMessage(), HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now());
    }
    @ExceptionHandler({WrongCredentialsException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public ErrorMsg missingFields(Exception e){
        log.error("exception: "+e);
        return new ErrorMsg(e.getMessage(),HttpStatus.NOT_FOUND.value(),LocalDateTime.now());
    }
    @ExceptionHandler({BaseException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorMsg handleMyErrors(Exception e){
        log.error("exception: "+e);
        return new ErrorMsg(e.getMessage(), HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
    }

    @ExceptionHandler({Exception.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMsg handleOtherErrors(Exception e){
        log.error("exception: "+e);
        return new ErrorMsg(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value(), LocalDateTime.now());
    }

    @ExceptionHandler({ParseException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public ErrorMsg dateParser(Exception e){
        return new ErrorMsg(e.getMessage(),HttpStatus.BAD_REQUEST.value(),LocalDateTime.now());
    }

    protected void validateLogin(HttpSession session) throws NotLoggedException{
        if(session.getAttribute("user") == null){
            throw new NotLoggedException();
        }
    }

    protected void validateLoginAdmin(HttpSession session) throws NotAdminException, NotLoggedException{
        if(session.getAttribute("user") == null){
            throw new NotLoggedException();
        }
        else{
            User logged = (User) (session.getAttribute("user"));
            if(!logged.isAdmin()){
                throw new NotAdminException();
            }
        }
    }
}