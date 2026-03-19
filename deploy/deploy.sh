# cloudflared tunnel login

cloudflared tunnel create pawn-service

cloudflared tunnel route dns pawn-service api.baotrang.io.vn

cloudflared tunnel route dns pawn-service minio.baotrang.io.vn
cloudflared tunnel route dns pawn-service minio-ui.baotrang.io.vn

cloudflared tunnel route dns pawn-service jenkins.baotrang.io.vn

cloudflared tunnel run pawn-service