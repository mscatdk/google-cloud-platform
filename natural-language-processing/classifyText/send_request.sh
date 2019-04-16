if [ $# -eq 0 ]
  then
    echo "Provide API key as first argument"
  else
    curl "https://language.googleapis.com/v1/documents:classifyText?key=${1}" -s -X POST -H "Content-Type: application/json" --data-binary @request.json
fi