# LiteLLM 官方镜像缺少 Pillow，而它的 ollama provider 在转换图片（多模态）时依赖它：
#   litellm.APIConnectionError: ollama image conversion failed please run `pip install Pillow`
# 该镜像用 venv 管理依赖且未自带 pip，因此先用 ensurepip 装上 pip 再安装 Pillow。
FROM ghcr.io/berriai/litellm:main-stable

RUN python3 -m ensurepip && python3 -m pip install --no-cache-dir Pillow
