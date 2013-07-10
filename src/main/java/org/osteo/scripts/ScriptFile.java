package org.osteo.scripts;

import java.io.File;
import java.io.IOException;

public class ScriptFile {

    public enum Status {

        ERROR, PROCESSED, IN_PROGRESS, NONE;
    }
    protected File file;
    protected Status status;

    public ScriptFile(File file, Status status) {
        this.file = file;
        this.status = status;
    }

    public File getFile() {
        return this.file;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return this.status;
    }

    public boolean is(Status status) {
        return this.status == status;
    }

    /**
     * Returns true if and only if both 'ScriptFile' objects have the same
     * absolute path.
     */
    @Override
    public boolean equals(Object file) {
        try {
            if (file instanceof ScriptFile
                    && ((ScriptFile) file).getFile().getAbsoluteFile().getCanonicalPath()
                    .equals(this.file.getAbsoluteFile().getCanonicalPath())) {
                return true;
            }
            if (file instanceof File
                    && ((File) file).getAbsoluteFile().getCanonicalPath()
                    .equals(this.file.getAbsoluteFile().getCanonicalPath())) {
                return true;
            }
        } catch (IOException e) {
        }
        return false;
    }
}
