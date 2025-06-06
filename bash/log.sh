_ERROR_COLOR="\033[0;31m"
_SUCCESS_COLOR="\033[0;32m"
_INFO_COLOR="\033[0;34m"
_RESET_COLOR="\033[0m"

function log() {
	printf "%s\n" "$1" >&2
}

function log_error() {
	printf "${_ERROR_COLOR}ERROR: %s${_RESET_COLOR}\n" "$1" >&2
}
function log_success() {
	printf "${_SUCCESS_COLOR}SUCCESS: %s${_RESET_COLOR}\n" "$1" >&2
}

function log_info() {
	printf "${_INFO_COLOR}INFO: %s${_RESET_COLOR}\n" "$1" >&2
}
