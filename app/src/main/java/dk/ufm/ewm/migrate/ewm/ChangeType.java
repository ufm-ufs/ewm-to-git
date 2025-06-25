package dk.ufm.ewm.migrate.ewm;

import com.ibm.team.scm.common.IChange;

public enum ChangeType {
    
    ADD,
    DELETE,
    MODIFY,
    RENAME,
    REPARENT,
    NONE,
    MODIFY_RENAME,
    MODIFY_REPARENT,
    RENAME_REPARENT,
    MODIFY_RENAME_REPARENT,
    MERGE
    ;
    
    public static ChangeType getChangeType(IChange change) {
        switch (change.kind()) {
        case IChange.NONE: {
            if (change.mergeStates().size() > 0) {
                return MERGE;
            }
            return NONE;
        }
        case IChange.ADD:
            return ADD;
        case IChange.DELETE:
            return DELETE;
        case IChange.MODIFY:
            return MODIFY;
        case IChange.RENAME:
            return RENAME;
        case IChange.REPARENT:
            return REPARENT;
        case IChange.MODIFY | IChange.RENAME:
            return MODIFY_RENAME;
        case IChange.MODIFY | IChange.REPARENT:
            return MODIFY_REPARENT;
        case IChange.RENAME | IChange.REPARENT:
            return RENAME_REPARENT;
        case IChange.MODIFY | IChange.RENAME | IChange.REPARENT:
            return MODIFY_RENAME_REPARENT;
        default:
            return NONE;
        }
    }    

}
