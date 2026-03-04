kubectl proxy \
  --port=8001 \
  --address=0.0.0.0 \
  --accept-hosts='^home$,^192\.168\.27\.65$,^localhost$,^127\.0\.0\.1$'